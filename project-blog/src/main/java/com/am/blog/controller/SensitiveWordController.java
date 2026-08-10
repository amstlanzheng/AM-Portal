package com.am.blog.controller;

import com.am.blog.entity.SensitiveWord;
import com.am.blog.service.SensitiveWordService;
import com.am.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "敏感词管理")
@RestController
@RequestMapping("/api/admin/sensitive-words")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @Operation(summary = "敏感词列表")
    @GetMapping
    public Result<Page<SensitiveWord>> listWords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean enabled) {
        return Result.success(sensitiveWordService.listWords(pageNum, pageSize, category, enabled));
    }

    @Operation(summary = "新增敏感词")
    @PostMapping
    public Result<Void> addWord(@RequestBody SensitiveWord word) {
        sensitiveWordService.addWord(word);
        return Result.success();
    }

    @Operation(summary = "修改敏感词")
    @PutMapping
    public Result<Void> updateWord(@RequestBody SensitiveWord word) {
        sensitiveWordService.updateWord(word);
        return Result.success();
    }

    @Operation(summary = "删除敏感词")
    @DeleteMapping("/{id}")
    public Result<Void> deleteWord(@PathVariable Long id) {
        sensitiveWordService.deleteWord(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用敏感词")
    @PutMapping("/{id}/enabled")
    public Result<Void> setEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        sensitiveWordService.setEnabled(id, enabled);
        return Result.success();
    }

    @Operation(summary = "所有启用的敏感词(供检测用)")
    @GetMapping("/all-enabled")
    public Result<List<String>> allEnabledWords() {
        return Result.success(sensitiveWordService.getAllEnabledWords());
    }

    @Operation(summary = "批量新增敏感词")
    @PostMapping("/batch")
    public Result<Void> batchAdd(@RequestBody List<SensitiveWord> words) {
        for (SensitiveWord word : words) {
            try {
                sensitiveWordService.addWord(word);
            } catch (Exception ignored) {
                // 跳过已存在的
            }
        }
        return Result.success();
    }
}
