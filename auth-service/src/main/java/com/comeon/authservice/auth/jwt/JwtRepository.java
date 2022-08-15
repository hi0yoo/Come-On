package com.comeon.authservice.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtRepository {

    private static final String AT_PREFIX = "AT_";
    private static final String UID_PREFIX = "UID_";

    private final RedisTemplate<String, String> redisTemplate;

    public void addBlackList(String accessToken, Duration duration) {
        addValues(AT_PREFIX + accessToken, accessToken, duration);
    }

    public void addRefreshToken(String userId, String refreshToken, Duration duration) {
        addValues(UID_PREFIX + userId, refreshToken, duration);
    }

    private void addValues(String key, String data, Duration duration) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    public Optional<String> findAccessToken(String accessToken) {
        return Optional.ofNullable(getValues(AT_PREFIX + accessToken));
    }

    public Optional<String> findRefreshTokenByUserId(String userId) {
        return Optional.ofNullable(getValues(UID_PREFIX + userId));
    }

    private String getValues(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(key);
    }

    private void removeValues(String key) {
        redisTemplate.delete(key);
    }

    public void removeRefreshToken(String userId) {
        removeValues(UID_PREFIX + userId);
    }
}
