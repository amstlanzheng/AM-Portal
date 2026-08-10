package com.am.user.service;

import com.am.common.constant.CommonConstant;
import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.am.common.util.RedisUtil;
import com.am.user.entity.User;
import com.am.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;

    public Page<User> listUsers(int pageNum, int pageSize, String status, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public User getUserByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    public void approveUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        user.setStatus(CommonConstant.USER_STATUS_ENABLED);
        updateById(user);
    }

    public void disableUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        user.setStatus(CommonConstant.USER_STATUS_DISABLED);
        updateById(user);
    }

    public void setAdmin(Long userId, Boolean isAdmin) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        user.setIsAdmin(isAdmin);
        updateById(user);
    }

    public void updateProfile(Long userId, String nickname, String email, String phone) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname);
        }
        if (StringUtils.hasText(email)) {
            user.setEmail(email);
        }
        if (StringUtils.hasText(phone)) {
            user.setPhone(phone);
        }
        updateById(user);
    }

    public void updateAvatar(Long userId, String avatarUrl) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        user.setAvatar(avatarUrl);
        updateById(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        if (newPassword.length() < 6) {
            throw new BusinessException(ResultCode.PASSWORD_TOO_SHORT);
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_SAME);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
        invalidateUserTokens(userId);
    }

    public void adminChangePassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (newPassword.length() < 6) {
            throw new BusinessException(ResultCode.PASSWORD_TOO_SHORT);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
        invalidateUserTokens(userId);
    }

    public void resetPassword(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(CommonConstant.INITIAL_PASSWORD));
        updateById(user);
        invalidateUserTokens(userId);
    }

    private void invalidateUserTokens(Long userId) {
        redisUtil.delete(CommonConstant.REDIS_TOKEN_PREFIX + "access:" + userId);
        redisUtil.delete(CommonConstant.REDIS_TOKEN_PREFIX + "refresh:" + userId);
    }
}
