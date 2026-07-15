package com.am.ai.controller;

import com.am.ai.entity.Prompt;
import com.am.ai.service.PromptService;
import com.am.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Prompt 管理")
@RestController
@RequestMapping("/api/admin/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    @Operation(summary = "Prompt 列表(管理员)")
    @GetMapping
    public Result<Page<Prompt>> listPrompts(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(promptService.listPrompts(pageNum, pageSize));
    }

    @Operation(summary = "启用的 Prompt 列表")
    @GetMapping("/enabled")
    public Result<List<Prompt>> listEnabledPrompts() {
        return Result.success(promptService.listEnabledPrompts());
    }

    @Operation(summary = "新增 Prompt(管理员)")
    @PostMapping
    public Result<Void> addPrompt(@RequestBody Prompt prompt) {
        promptService.addPrompt(prompt);
        return Result.success();
    }

    @Operation(summary = "修改 Prompt(管理员)")
    @PutMapping
    public Result<Void> updatePrompt(@RequestBody Prompt prompt) {
        promptService.updatePrompt(prompt);
        return Result.success();
    }

    @Operation(summary = "删除 Prompt(管理员)")
    @DeleteMapping("/{id}")
    public Result<Void> deletePrompt(@PathVariable Long id) {
        promptService.deletePrompt(id);
        return Result.success();
    }

    @Operation(summary = "启用/停用 Prompt(管理员)")
    @PutMapping("/{id}/enabled")
    public Result<Void> setEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        promptService.setEnabled(id, enabled);
        return Result.success();
    }

    @Operation(summary = "设置默认 Prompt(管理员)")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        promptService.setDefault(id);
        return Result.success();
    }
}
