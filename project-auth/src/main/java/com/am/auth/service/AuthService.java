package com.am.auth.service;

import com.am.auth.dto.LoginRequest;
import com.am.auth.dto.RegisterRequest;
import com.am.auth.dto.TokenResponse;
import com.am.common.constant.CommonConstant;
import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.am.common.util.JwtUtil;
import com.am.common.util.RedisUtil;
import com.am.user.entity.User;
import com.am.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public TokenResponse login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        if (CommonConstant.USER_STATUS_PENDING.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_PENDING);
        }
        if (CommonConstant.USER_STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        return generateTokens(user);
    }

    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setStatus(CommonConstant.USER_STATUS_PENDING);
        user.setIsAdmin(false);
        userMapper.insert(user);
    }

    public TokenResponse refresh(String refreshToken) {
        try {
            var claims = JwtUtil.parseToken(jwtSecret, refreshToken);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // 验证 Redis 中的 refresh token
            String storedToken = redisUtil.get(CommonConstant.REDIS_TOKEN_PREFIX + "refresh:" + userId);
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            User user = userMapper.selectById(Long.valueOf(userId));
            if (user == null) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            return generateTokens(user);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    private TokenResponse generateTokens(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getIsAdmin() ? CommonConstant.ROLE_ADMIN : CommonConstant.ROLE_USER);

        String accessToken = JwtUtil.generateToken(jwtSecret, String.valueOf(user.getId()), claims, accessTokenExpiration);
        String refreshToken = JwtUtil.generateToken(jwtSecret, String.valueOf(user.getId()), claims, refreshTokenExpiration);

        // 存储 token 到 Redis
        redisUtil.set(CommonConstant.REDIS_TOKEN_PREFIX + "access:" + user.getId(), accessToken,
                accessTokenExpiration, TimeUnit.MILLISECONDS);
        redisUtil.set(CommonConstant.REDIS_TOKEN_PREFIX + "refresh:" + user.getId(), refreshToken,
                refreshTokenExpiration, TimeUnit.MILLISECONDS);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .build();
    }
}
