package com.am.admin.controller;

import com.am.common.result.Result;
import com.am.user.service.UserService;
import com.am.project.service.ProjectService;
import com.am.blog.service.BlogService;
import com.am.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserService userService;
    private final ProjectService projectService;
    private final BlogService blogService;

    @GetMapping
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userService.count());
        data.put("projectCount", projectService.count());
        data.put("blogCount", blogService.count());
        return Result.success(data);
    }
}
