package com.am.api.controller;

import com.am.common.result.Result;
import com.am.blog.entity.Blog;
import com.am.project.entity.Project;
import com.am.project.service.ProjectService;
import com.am.blog.service.BlogService;
import com.am.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final SysConfigService sysConfigService;
    private final ProjectService projectService;
    private final BlogService blogService;

    @GetMapping
    public Result<Map<String, Object>> getHomePage() {
        Map<String, Object> data = new HashMap<>();
        data.put("siteInfo", sysConfigService.getHomePageInfo());
        data.put("recentProjects", projectService.getRecentProjects(5).getRecords());
        data.put("recentBlogs", blogService.getRecentBlogs(5).getRecords());
        return Result.success(data);
    }

    @Operation(summary = "站点信息(公开)")
    @GetMapping("/site-info")
    public Result<Map<String, String>> getSiteInfo() {
        return Result.success(sysConfigService.getHomePageInfo());
    }

    @Operation(summary = "最近项目(公开)")
    @GetMapping("/recent-projects")
    public Result<List<Project>> getRecentProjects(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(projectService.getRecentProjects(limit).getRecords());
    }

    @Operation(summary = "最近博客(公开)")
    @GetMapping("/recent-blogs")
    public Result<List<Blog>> getRecentBlogs(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(blogService.getRecentBlogs(limit).getRecords());
    }
}
