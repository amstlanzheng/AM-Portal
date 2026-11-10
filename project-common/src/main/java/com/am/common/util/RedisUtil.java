package com.am.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    public void set(@NonNull String key, @NonNull String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(@NonNull String key, @NonNull String value, long timeout, @NonNull TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Nullable
    public String get(@NonNull String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(@NonNull String key) {
        return redisTemplate.delete(key);
    }

    public Boolean hasKey(@NonNull String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(@NonNull String key, long timeout, @NonNull TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(@NonNull String key, @NonNull TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }
}
