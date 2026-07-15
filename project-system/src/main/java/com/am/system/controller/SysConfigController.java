package com.am.system.controller;

import com.am.common.result.Result;
import com.am.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "系统配置管理")
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "获取首页信息")
    @GetMapping("/home")
    public Result<Map<String, String>> getHomePageInfo() {
        return Result.success(sysConfigService.getHomePageInfo());
    }

    @Operation(summary = "修改首页信息(管理员)")
    @PutMapping("/home")
    public Result<Void> updateHomePageInfo(@RequestBody Map<String, String> configs) {
        sysConfigService.updateHomePageInfo(configs);
        return Result.success();
    }

    @Operation(summary = "获取所有配置(管理员)")
    @GetMapping("/all")
    public Result<Map<String, String>> getAllConfigs() {
        return Result.success(sysConfigService.getAllConfigs());
    }

    @Operation(summary = "修改配置(管理员)")
    @PutMapping("/{key}")
    public Result<Void> setConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        sysConfigService.setConfig(key, body.get("value"));
        return Result.success();
    }
}
