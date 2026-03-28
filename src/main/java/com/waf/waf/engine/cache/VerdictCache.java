package com.waf.waf.engine.cache;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.waf.waf.dto.WafResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VerdictCache {

    private final StringRedisTemplate redisTemplate;
    private static final String VERDICT_PREFIX = "verdict:";

    private String key(String requestHash) {
        return VERDICT_PREFIX + requestHash;
    }

    public WafResult get(String requestHash) {
        String cached = redisTemplate.opsForValue().get(key(requestHash));
        if (cached != null) {
            // FIX: wrap in fromCache() so WafFilter knows not to re-record violation
            return WafResult.fromCache(WafResult.fromString(cached));
        }
        return null;
    }

    public void put(String requestHash, WafResult result, Duration ttl) {
        redisTemplate.opsForValue().set(key(requestHash), result.toString(), ttl);
    }
}