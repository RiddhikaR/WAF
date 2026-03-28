package com.waf.waf.engine.cache;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReplayAttackCache {

    private final StringRedisTemplate redisTemplate;
    private static final String REPLAY_PREFIX = "replay:";

    private String key(String requestHash) {
        return REPLAY_PREFIX + requestHash;
    }

    public boolean isReplay(String requestHash) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(requestHash)));
    }

    public void record(String requestHash, Duration ttl) {
        redisTemplate.opsForValue().set(key(requestHash), "1", ttl);
    }
}