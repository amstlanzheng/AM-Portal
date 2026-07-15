package com.am.user.controller;

import com.am.common.result.Result;
import com.am.framework.security.UserPrincipal;
import com.am.user.entity.User;
import com.am.user.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getById(principal.getId());
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/me/profile")
    public Result<Void> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                      @RequestParam(required = false) String nickname,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String phone) {
        userService.updateProfile(principal.getId(), nickname, email, phone);
        return Result.success();
    }

    @Operation(summary = "修改头像")
    @PutMapping("/me/avatar")
    public Result<Void> updateAvatar(@AuthenticationPrincipal UserPrincipal principal,
                                     @RequestParam String avatarUrl) {
        userService.updateAvatar(principal.getId(), avatarUrl);
        return Result.success();
    }

    // ===== 管理员接口 =====

    @Operation(summary = "用户列表(管理员)")
    @GetMapping("/admin/list")
    public Result<Page<User>> listUsers(@RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String keyword) {
        Page<User> page = userService.listUsers(pageNum, pageSize, status, keyword);
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(page);
    }

    @Operation(summary = "审核通过(管理员)")
    @PutMapping("/admin/{userId}/approve")
    public Result<Void> approveUser(@PathVariable Long userId) {
        userService.approveUser(userId);
        return Result.success();
    }

    @Operation(summary = "禁用用户(管理员)")
    @PutMapping("/admin/{userId}/disable")
    public Result<Void> disableUser(@PathVariable Long userId) {
        userService.disableUser(userId);
        return Result.success();
    }

    @Operation(summary = "设置管理员(管理员)")
    @PutMapping("/admin/{userId}/admin")
    public Result<Void> setAdmin(@PathVariable Long userId, @RequestParam Boolean isAdmin) {
        userService.setAdmin(userId, isAdmin);
        return Result.success();
    }
}
