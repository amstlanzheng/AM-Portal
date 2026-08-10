package com.am.blog.service;

import com.am.blog.entity.SensitiveWord;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.am.blog.mapper.SensitiveWordMapper;
import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensitiveWordService extends ServiceImpl<SensitiveWordMapper, SensitiveWord> {

    public Page<SensitiveWord> listWords(int pageNum, int pageSize, String category, Boolean enabled) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(SensitiveWord::getCategory, category);
        }
        if (enabled != null) {
            wrapper.eq(SensitiveWord::getIsEnabled, enabled);
        }
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public void addWord(SensitiveWord word) {
        // 检查是否已存在
        long count = count(new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getWord, word.getWord()));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_EXISTS.getCode(), "敏感词已存在: " + word.getWord());
        }
        save(word);
    }

    public void updateWord(SensitiveWord word) {
        SensitiveWord existing = getById(word.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        // 检查词是否与其他记录重复
        long count = count(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getWord, word.getWord())
                .ne(SensitiveWord::getId, word.getId()));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_EXISTS.getCode(), "敏感词已存在: " + word.getWord());
        }
        updateById(word);
    }

    public void deleteWord(Long id) {
        removeById(id);
    }

    public void setEnabled(Long id, Boolean enabled) {
        SensitiveWord word = getById(id);
        if (word == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        word.setIsEnabled(enabled);
        updateById(word);
    }

    /**
     * 获取所有启用的敏感词列表
     */
    public List<String> getAllEnabledWords() {
        return list(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getIsEnabled, true)
                .select(SensitiveWord::getWord))
                .stream()
                .map(SensitiveWord::getWord)
                .collect(Collectors.toList());
    }
}
