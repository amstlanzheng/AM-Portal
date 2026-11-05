package com.am.system.controller;

import com.am.common.result.Result;
import com.am.system.entity.DailyTip;
import com.am.system.service.DailyTipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "每日小贴士")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DailyTipController {

    private final DailyTipService dailyTipService;

    @Operation(summary = "随机小贴士(公开)")
    @GetMapping("/home/tips/random")
    public Result<DailyTip> getRandomTip() {
        return Result.success(dailyTipService.randomEnabled());
    }

    @Operation(summary = "小贴士列表(管理员)")
    @GetMapping("/admin/tips")
    public Result<List<DailyTip>> listTips() {
        return Result.success(dailyTipService.listAll());
    }

    @Operation(summary = "新增小贴士(管理员)")
    @PostMapping("/admin/tips")
    public Result<Void> addTip(@RequestBody Map<String, String> body) {
        dailyTipService.add(body.get("content"));
        return Result.success();
    }

    @Operation(summary = "删除小贴士(管理员)")
    @DeleteMapping("/admin/tips/{id}")
    public Result<Void> deleteTip(@PathVariable Long id) {
        dailyTipService.removeById(id);
        return Result.success();
    }
}
