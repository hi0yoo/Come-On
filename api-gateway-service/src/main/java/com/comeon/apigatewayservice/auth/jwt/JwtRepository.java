package com.comeon.apigatewayservice.auth.jwt;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JwtRepository {

    private static final String AT_PREFIX = "BLACKLIST_";
    private static final String UID_PREFIX = "UID_";

    private final RedisTemplate<String, String> redisTemplate;

    public JwtRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
}
