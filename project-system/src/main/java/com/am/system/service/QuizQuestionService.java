package com.am.system.service;

import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.am.system.entity.QuizQuestion;
import com.am.system.mapper.QuizQuestionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * MBTI 测试题目：题库维护 + 公开取题
 */
@Service
@RequiredArgsConstructor
public class QuizQuestionService extends ServiceImpl<QuizQuestionMapper, QuizQuestion> {

    private final ObjectMapper objectMapper;

    /** 公开：启用题目按 sort_order */
    public List<QuizQuestion> listEnabled() {
        return list(new LambdaQueryWrapper<QuizQuestion>()
                .eq(QuizQuestion::getIsEnabled, true)
                .orderByAsc(QuizQuestion::getSortOrder)
                .orderByAsc(QuizQuestion::getId));
    }

    /** 后台：全部题目 */
    public List<QuizQuestion> listAll() {
        return list(new LambdaQueryWrapper<QuizQuestion>()
                .orderByAsc(QuizQuestion::getSortOrder)
                .orderByAsc(QuizQuestion::getId));
    }

    public void add(QuizQuestion question) {
        validate(question);
        save(question);
    }

    public void updateQuestion(QuizQuestion question) {
        if (question.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        validate(question);
        updateById(question);
    }

    /** 校验：题干非空；options 必须是合法选项数组（每项含 text 与 scores 对象） */
    private void validate(QuizQuestion question) {
        if (!StringUtils.hasText(question.getQuestionText())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        String options = question.getOptions();
        if (!StringUtils.hasText(options)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        try {
            JsonNode arr = objectMapper.readTree(options);
            if (!arr.isArray() || arr.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            for (JsonNode opt : arr) {
                // 每项必须含非空 text 与 object 类型 scores
                if (opt.path("text").asText().isBlank() || !opt.path("scores").isObject()) {
                    throw new BusinessException(ResultCode.PARAM_ERROR);
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
