package com.am.storage.controller;

import com.am.common.result.Result;
import com.am.framework.security.UserPrincipal;
import com.am.storage.entity.FileInfo;
import com.am.storage.service.MinioService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    @Operation(summary = "上传文件")
    @PostMapping("/files/upload")
    public Result<FileInfo> uploadFile(@RequestParam MultipartFile file,
                                       @RequestParam(defaultValue = "other") String directory,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        FileInfo fileInfo = minioService.uploadFile(file, directory, principal.getId());
        return Result.success(fileInfo);
    }

    @Operation(summary = "删除文件(管理员)")
    @DeleteMapping("/admin/files/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        minioService.deleteFile(fileId);
        return Result.success();
    }

    @Operation(summary = "文件列表(管理员)")
    @GetMapping("/admin/files")
    public Result<Page<FileInfo>> listFiles(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String directory,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(minioService.listFiles(pageNum, pageSize, directory, keyword));
    }
}
