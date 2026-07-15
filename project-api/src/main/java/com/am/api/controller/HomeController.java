package com.am.api.controller;

import com.am.common.result.Result;
import com.am.project.service.ProjectService;
import com.am.blog.service.BlogService;
import com.am.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
}
