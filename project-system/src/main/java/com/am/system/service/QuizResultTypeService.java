package com.am.system.service;

import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.am.system.entity.QuizResultType;
import com.am.system.mapper.QuizResultTypeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * MBTI 测试结果类型：类型维护 + 公开取全量
 */
@Service
public class QuizResultTypeService extends ServiceImpl<QuizResultTypeMapper, QuizResultType> {

    /** 公开：全部结果类型按 sort_order */
    public List<QuizResultType> listAll() {
        return list(new LambdaQueryWrapper<QuizResultType>()
                .orderByAsc(QuizResultType::getSortOrder)
                .orderByAsc(QuizResultType::getId));
    }

    public void add(QuizResultType type) {
        validate(type);
        if (existsCode(type.getCode(), null)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        save(type);
    }

    public void updateType(QuizResultType type) {
        if (type.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        validate(type);
        if (existsCode(type.getCode(), type.getId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        updateById(type);
    }

    private void validate(QuizResultType type) {
        if (!StringUtils.hasText(type.getCode()) || !StringUtils.hasText(type.getName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /** code 是否已被其他记录占用 */
    private boolean existsCode(String code, Long excludeId) {
        LambdaQueryWrapper<QuizResultType> wrapper = new LambdaQueryWrapper<QuizResultType>()
                .eq(QuizResultType::getCode, code);
        if (excludeId != null) {
            wrapper.ne(QuizResultType::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
