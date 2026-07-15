package com.am.ai.service;

import com.am.ai.entity.Prompt;
import com.am.ai.mapper.PromptMapper;
import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromptService extends ServiceImpl<PromptMapper, Prompt> {

    public String getDefaultPrompt() {
        Prompt prompt = getOne(new LambdaQueryWrapper<Prompt>()
                .eq(Prompt::getIsDefault, true)
                .eq(Prompt::getIsEnabled, true));
        return prompt != null ? prompt.getContent() : null;
    }

    public Page<Prompt> listPrompts(int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Prompt>().orderByDesc(Prompt::getCreateTime));
    }

    public List<Prompt> listEnabledPrompts() {
        return list(new LambdaQueryWrapper<Prompt>()
                .eq(Prompt::getIsEnabled, true)
                .orderByDesc(Prompt::getCreateTime));
    }

    public void addPrompt(Prompt prompt) {
        if (prompt.getIsDefault() != null && prompt.getIsDefault()) {
            clearDefaultPrompt();
        }
        save(prompt);
    }

    @Transactional
    public void updatePrompt(Prompt prompt) {
        Prompt existing = getById(prompt.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (prompt.getIsDefault() != null && prompt.getIsDefault()) {
            clearDefaultPrompt();
        }
        updateById(prompt);
    }

    public void deletePrompt(Long id) {
        Prompt prompt = getById(id);
        if (prompt == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        removeById(id);
    }

    public void setEnabled(Long id, Boolean enabled) {
        Prompt prompt = getById(id);
        if (prompt == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        prompt.setIsEnabled(enabled);
        updateById(prompt);
    }

    @Transactional
    public void setDefault(Long id) {
        Prompt prompt = getById(id);
        if (prompt == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        clearDefaultPrompt();
        prompt.setIsDefault(true);
        updateById(prompt);
    }

    private void clearDefaultPrompt() {
        lambdaUpdate()
                .eq(Prompt::getIsDefault, true)
                .set(Prompt::getIsDefault, false)
                .update();
    }
}
