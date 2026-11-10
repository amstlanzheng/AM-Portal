package com.am.system.controller;

import com.am.common.result.Result;
import com.am.system.entity.QuizQuestion;
import com.am.system.entity.QuizResultType;
import com.am.system.service.QuizQuestionService;
import com.am.system.service.QuizResultTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MBTI 变体测试：公开取题/取结果类型 + 后台题库维护
 */
@Tag(name = "MBTI测试")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizQuestionService quizQuestionService;
    private final QuizResultTypeService quizResultTypeService;

    /* ---------- 公开接口（/api/home/** 已 permitAll） ---------- */

    @Operation(summary = "获取启用题目(公开)")
    @GetMapping("/home/quiz/questions")
    public Result<List<QuizQuestion>> listEnabledQuestions() {
        return Result.success(quizQuestionService.listEnabled());
    }

    @Operation(summary = "获取结果类型(公开)")
    @GetMapping("/home/quiz/result-types")
    public Result<List<QuizResultType>> listResultTypes() {
        return Result.success(quizResultTypeService.listAll());
    }

    /* ---------- 管理接口（/api/admin/** 既有鉴权规则） ---------- */

    @Operation(summary = "题目列表(管理员)")
    @GetMapping("/admin/quiz/questions")
    public Result<List<QuizQuestion>> listQuestions() {
        return Result.success(quizQuestionService.listAll());
    }

    @Operation(summary = "新增题目(管理员)")
    @PostMapping("/admin/quiz/questions")
    public Result<Void> addQuestion(@RequestBody QuizQuestion question) {
        quizQuestionService.add(question);
        return Result.success();
    }

    @Operation(summary = "修改题目(管理员)")
    @PutMapping("/admin/quiz/questions")
    public Result<Void> updateQuestion(@RequestBody QuizQuestion question) {
        quizQuestionService.updateQuestion(question);
        return Result.success();
    }

    @Operation(summary = "删除题目(管理员)")
    @DeleteMapping("/admin/quiz/questions/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        quizQuestionService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "结果类型列表(管理员)")
    @GetMapping("/admin/quiz/result-types")
    public Result<List<QuizResultType>> listTypes() {
        return Result.success(quizResultTypeService.listAll());
    }

    @Operation(summary = "新增结果类型(管理员)")
    @PostMapping("/admin/quiz/result-types")
    public Result<Void> addResultType(@RequestBody QuizResultType type) {
        quizResultTypeService.add(type);
        return Result.success();
    }

    @Operation(summary = "修改结果类型(管理员)")
    @PutMapping("/admin/quiz/result-types")
    public Result<Void> updateResultType(@RequestBody QuizResultType type) {
        quizResultTypeService.updateType(type);
        return Result.success();
    }

    @Operation(summary = "删除结果类型(管理员)")
    @DeleteMapping("/admin/quiz/result-types/{id}")
    public Result<Void> deleteResultType(@PathVariable Long id) {
        quizResultTypeService.removeById(id);
        return Result.success();
    }
}
