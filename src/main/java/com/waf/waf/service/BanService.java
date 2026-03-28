package com.waf.waf.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.waf.waf.config.WafProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BanService {

    private final StringRedisTemplate redisTemplate;
    private final WafProperties wafProperties;

    private static final String BAN_PREFIX = "ban:";
    private static final String VIOLATION_PREFIX = "violation:";

    private String banKey(String ip) { return BAN_PREFIX + ip; }
    private String violationKey(String ip) { return VIOLATION_PREFIX + ip; }

    public boolean isBanned(String ip) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(banKey(ip)));
    }

    public void recordViolationVerdict(String ip, int statusCode, String message) {
        recordViolationVerdict(ip, statusCode);
        log.warn("Blocked request message: {}", message);
    }

    public void recordViolationVerdict(String ip, int statusCode) {
        int maxViolations = wafProperties.getBan().getMaxViolations();
        Duration violationWindow = Duration.ofMinutes(wafProperties.getBan().getViolationWindowMinutes());
        Duration banDuration = Duration.ofMinutes(wafProperties.getBan().getBanDurationMinutes());

        String vKey = violationKey(ip);
        Long count = redisTemplate.opsForValue().increment(vKey);

        if (count != null && count == 1) {
            redisTemplate.expire(vKey, violationWindow);
        }

        log.warn("Violation | IP: {} | Status: {} | Count: {}", ip, statusCode, count);

        if (count != null && count >= maxViolations) {
            redisTemplate.opsForValue().set(banKey(ip), "1", banDuration);
            redisTemplate.delete(vKey);
            log.error("IP BANNED | IP: {} | Duration: {} mins", ip, banDuration.toMinutes());
        }
    }

    // Admin: get current violation count for an IP
    public int getViolationCount(String ip) {
        String val = redisTemplate.opsForValue().get(violationKey(ip));
        return val != null ? Integer.parseInt(val) : 0;
    }

    // Admin: manually unban an IP
    public boolean unban(String ip) {
        Boolean deleted = redisTemplate.delete(banKey(ip));
        if (Boolean.TRUE.equals(deleted)) {
            log.info("IP UNBANNED (manual) | IP: {}", ip);
            return true;
        }
        return false;
    }

    // Admin: get ban TTL in seconds (-1 = no expiry, -2 = not banned)
    public long getBanTtlSeconds(String ip) {
        Long ttl = redisTemplate.getExpire(banKey(ip));
        return ttl != null ? ttl : -2;
    }
}