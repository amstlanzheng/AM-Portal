package com.am.ai.controller;

import com.am.ai.entity.AiConversation;
import com.am.ai.entity.AiMessage;
import com.am.ai.service.AiService;
import com.am.common.result.Result;
import com.am.framework.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "AI 助手")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI 对话(流式)")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@AuthenticationPrincipal UserPrincipal principal,
                           @RequestBody Map<String, Object> body) {
        Long userId = principal != null ? principal.getId() : null;
        Long conversationId = body.get("conversationId") != null ?
                Long.valueOf(body.get("conversationId").toString()) : null;
        String message = body.get("message").toString();

        log.info("AI chat 请求: userId={}, conversationId={}, message={}", userId, conversationId, message);

        // 5分钟超时
        SseEmitter emitter = new SseEmitter(300000L);

        aiService.chat(userId, conversationId, message)
                .subscribe(
                        content -> {
                            if (!content.isEmpty()) {
                                try {
                                    emitter.send(SseEmitter.event().data(content));
                                    log.debug("SSE 发送 chunk: {}", content);
                                } catch (IOException e) {
                                    log.error("SSE 发送失败", e);
                                    emitter.completeWithError(e);
                                }
                            }
                        },
                        error -> {
                            log.error("AI 流式调用异常", error);
                            try {
                                emitter.send(SseEmitter.event().data("错误: " + error.getMessage()));
                            } catch (IOException ignored) {
                            }
                            emitter.completeWithError(error);
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().data("[DONE]"));
                                emitter.complete();
                                log.info("SSE 流完成");
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        }
                );

        return emitter;
    }

    @Operation(summary = "创建新对话")
    @PostMapping("/conversations")
    public Result<AiConversation> createConversation(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return Result.success(aiService.createConversation(userId));
    }

    @Operation(summary = "获取对话列表")
    @GetMapping("/conversations")
    public Result<List<AiConversation>> listConversations(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return Result.success(aiService.listConversations(userId));
    }

    @Operation(summary = "获取对话消息")
    @GetMapping("/conversations/{conversationId}/messages")
    public Result<List<AiMessage>> getMessages(@PathVariable Long conversationId) {
        return Result.success(aiService.getMessages(conversationId));
    }

    @Operation(summary = "删除对话")
    @DeleteMapping("/conversations/{conversationId}")
    public Result<Void> deleteConversation(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long conversationId) {
        Long userId = principal != null ? principal.getId() : null;
        aiService.deleteConversation(conversationId, userId);
        return Result.success();
    }
}
