package com.am.ai.service;

import com.am.ai.entity.AiConversation;
import com.am.ai.entity.AiMessage;
import com.am.ai.mapper.AiConversationMapper;
import com.am.ai.mapper.AiMessageMapper;
import com.am.common.constant.CommonConstant;
import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.am.common.util.RedisUtil;
import com.am.system.service.SysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService extends ServiceImpl<AiConversationMapper, AiConversation> {

    private final OpenAiChatModel chatModel;
    private final AiMessageMapper messageMapper;
    private final SysConfigService sysConfigService;
    private final RedisUtil redisUtil;
    private final PromptService promptService;

    public Flux<String> chat(Long userId, Long conversationId, String userMessage) {
        if (userId == null && !sysConfigService.isAiEnabled()) {
            throw new BusinessException(ResultCode.AI_NOT_AVAILABLE);
        }

        AiConversation conversation;
        if (conversationId != null) {
            conversation = getById(conversationId);
            if (conversation == null || (userId != null && !conversation.getUserId().equals(userId))) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND);
            }
        } else {
            conversation = new AiConversation();
            conversation.setUserId(userId != null ? userId : 0L);
            conversation.setTitle(userMessage.length() > 50 ? userMessage.substring(0, 50) : userMessage);
            save(conversation);
        }

        final Long convId = conversation.getId();

        AiMessage userMsg = new AiMessage();
        userMsg.setConversationId(convId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMsg);

        List<Message> messages = buildContext(convId, userMessage);
        Prompt prompt = new Prompt(messages);

        long startTime = System.currentTimeMillis();
        StringBuilder responseBuilder = new StringBuilder();

        return chatModel.stream(prompt)
                .map(chatResponse -> {
                    if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                        String content = chatResponse.getResult().getOutput().getContent();
                        if (content != null) {
                            responseBuilder.append(content);
                            return content;
                        }
                    }
                    return "";
                })
                .doOnComplete(() -> {
                    AiMessage assistantMsg = new AiMessage();
                    assistantMsg.setConversationId(convId);
                    assistantMsg.setRole("assistant");
                    assistantMsg.setContent(responseBuilder.toString());
                    assistantMsg.setCreateTime(LocalDateTime.now());
                    messageMapper.insert(assistantMsg);

                    updateRedisContext(convId, userMessage, responseBuilder.toString());

                    long duration = System.currentTimeMillis() - startTime;
                    log.info("AI 调用完成, conversationId={}, duration={}ms", convId, duration);
                });
    }

    public AiConversation createConversation(Long userId) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId != null ? userId : 0L);
        conversation.setTitle("新对话");
        save(conversation);
        return conversation;
    }

    public List<AiConversation> listConversations(Long userId) {
        return list(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .orderByDesc(AiConversation::getUpdateTime));
    }

    public List<AiMessage> getMessages(Long conversationId) {
        return messageMapper.selectList(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId)
                .orderByAsc(AiMessage::getCreateTime));
    }

    public void deleteConversation(Long conversationId, Long userId) {
        AiConversation conversation = getById(conversationId);
        if (conversation == null || (userId != null && !conversation.getUserId().equals(userId))) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        removeById(conversationId);
        messageMapper.delete(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId));
        redisUtil.delete(CommonConstant.REDIS_AI_CONTEXT_PREFIX + conversationId);
    }

    private List<Message> buildContext(Long conversationId, String currentUserMessage) {
        List<Message> messages = new ArrayList<>();

        String systemPrompt = promptService.getDefaultPrompt();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(new SystemMessage(systemPrompt));
        }

        String contextKey = CommonConstant.REDIS_AI_CONTEXT_PREFIX + conversationId;
        String context = redisUtil.get(contextKey);
        if (context != null && !context.isEmpty()) {
            String[] pairs = context.split("\\|\\|\\|");
            for (String pair : pairs) {
                String[] parts = pair.split(":::", 2);
                if (parts.length == 2) {
                    if ("user".equals(parts[0])) {
                        messages.add(new UserMessage(parts[1]));
                    } else if ("assistant".equals(parts[0])) {
                        messages.add(new AssistantMessage(parts[1]));
                    }
                }
            }
        }

        messages.add(new UserMessage(currentUserMessage));
        return messages;
    }

    private void updateRedisContext(Long conversationId, String userMessage, String assistantMessage) {
        String contextKey = CommonConstant.REDIS_AI_CONTEXT_PREFIX + conversationId;
        String newPair = "user:::" + userMessage + "|||assistant:::" + assistantMessage;

        String existing = redisUtil.get(contextKey);
        if (existing != null && !existing.isEmpty()) {
            String[] pairs = existing.split("\\|\\|\\|");
            int maxRounds = CommonConstant.MAX_AI_CONTEXT_ROUNDS;
            if (pairs.length / 2 >= maxRounds) {
                int startIndex = (pairs.length / 2 - maxRounds + 1) * 2;
                StringBuilder sb = new StringBuilder();
                for (int i = startIndex; i < pairs.length; i++) {
                    if (sb.length() > 0) sb.append("|||");
                    sb.append(pairs[i]);
                }
                sb.append("|||").append(newPair);
                redisUtil.set(contextKey, sb.toString(), 2, TimeUnit.HOURS);
            } else {
                redisUtil.set(contextKey, existing + "|||" + newPair, 2, TimeUnit.HOURS);
            }
        } else {
            redisUtil.set(contextKey, newPair, 2, TimeUnit.HOURS);
        }
    }
}
