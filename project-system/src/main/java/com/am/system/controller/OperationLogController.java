package com.am.system.controller;

import com.am.common.result.Result;
import com.am.system.entity.OperationLog;
import com.am.system.service.OperationLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "日志列表(管理员)")
    @GetMapping
    public Result<Page<OperationLog>> listLogs(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) String operationType,
                                                @RequestParam(required = false) Long userId) {
        return Result.success(operationLogService.listLogs(pageNum, pageSize, operationType, userId));
    }
}
