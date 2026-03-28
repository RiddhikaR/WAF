package com.waf.waf.service;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.waf.waf.config.WafProperties;
import com.waf.waf.engine.cache.RateLimitCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitCache cache;
    private final WafProperties wafProperties;

    public boolean allowed(String ip) {
        int threshold = wafProperties.getRateLimit().getThreshold();
        Duration window = Duration.ofSeconds(wafProperties.getRateLimit().getWindowSeconds());
        int count = cache.increment(ip, window);
        return count <= threshold;
    }

    public int remaining(String ip) {
        int threshold = wafProperties.getRateLimit().getThreshold();
        int current = cache.get(ip);
        return Math.max(0, threshold - current);
    }

    public void applyRateLimitHeaders(HttpHeaders headers, String ip) {
        int threshold = wafProperties.getRateLimit().getThreshold();
        int windowSeconds = wafProperties.getRateLimit().getWindowSeconds();
        headers.set("X-RateLimit-Limit", String.valueOf(threshold));
        headers.set("X-RateLimit-Remaining", String.valueOf(remaining(ip)));
        headers.set("X-RateLimit-Window", windowSeconds + "s");
    }
}