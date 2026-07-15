package com.am.project.controller;

import com.am.common.result.Result;
import com.am.project.entity.Project;
import com.am.project.service.ProjectService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "项目管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ===== 公开接口 =====

    @Operation(summary = "项目列表(公开)")
    @GetMapping("/projects")
    public Result<Page<Project>> listProjects(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(projectService.listProjects(pageNum, pageSize, true));
    }

    @Operation(summary = "项目详情(公开)")
    @GetMapping("/projects/{id}")
    public Result<Project> getProjectDetail(@PathVariable Long id) {
        return Result.success(projectService.getProjectDetail(id));
    }

    @Operation(summary = "最近项目(公开)")
    @GetMapping("/projects/recent")
    public Result<Page<Project>> getRecentProjects(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(projectService.getRecentProjects(limit));
    }

    // ===== 管理员接口 =====

    @Operation(summary = "项目列表(管理员)")
    @GetMapping("/admin/projects")
    public Result<Page<Project>> listAllProjects(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(projectService.listProjects(pageNum, pageSize, false));
    }

    @Operation(summary = "新增项目(管理员)")
    @PostMapping("/admin/projects")
    public Result<Void> addProject(@RequestBody Project project) {
        projectService.addProject(project);
        return Result.success();
    }

    @Operation(summary = "修改项目(管理员)")
    @PutMapping("/admin/projects")
    public Result<Void> updateProject(@RequestBody Project project) {
        projectService.updateProject(project);
        return Result.success();
    }

    @Operation(summary = "删除项目(管理员)")
    @DeleteMapping("/admin/projects/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success();
    }

    @Operation(summary = "置顶项目(管理员)")
    @PutMapping("/admin/projects/{id}/top")
    public Result<Void> setTop(@PathVariable Long id, @RequestParam Boolean isTop) {
        projectService.setTop(id, isTop);
        return Result.success();
    }

    @Operation(summary = "显示/隐藏项目(管理员)")
    @PutMapping("/admin/projects/{id}/visible")
    public Result<Void> setVisible(@PathVariable Long id, @RequestParam Boolean isVisible) {
        projectService.setVisible(id, isVisible);
        return Result.success();
    }
}
