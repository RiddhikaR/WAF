package com.waf.waf.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waf.waf.service.BanService;
import com.waf.waf.service.RateLimitService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

@RestController
@RequestMapping("/waf/admin")
@RequiredArgsConstructor
public class WafAdminController {

    private final BanService banService;
    private final RateLimitService rateLimitService;
    private final StringRedisTemplate redisTemplate;

    private static final String EVENTS_KEY = "waf:events";

    @GetMapping("/ban/{ip}")
    public ResponseEntity<Map<String, Object>> getBanStatus(@PathVariable String ip) {
        boolean banned = banService.isBanned(ip);
        long ttlSeconds = banService.getBanTtlSeconds(ip);
        int violations = banService.getViolationCount(ip);
        return ResponseEntity.ok(Map.of(
            "ip", ip,
            "banned", banned,
            "banTtlSeconds", ttlSeconds,
            "currentViolations", violations
        ));
    }

    @DeleteMapping("/ban/{ip}")
    public ResponseEntity<Map<String, Object>> unban(@PathVariable String ip) {
        boolean success = banService.unban(ip);
        return ResponseEntity.ok(Map.of(
            "ip", ip,
            "unbanned", success
        ));
    }

    @GetMapping("/rate/{ip}")
    public ResponseEntity<Map<String, Object>> getRateStatus(@PathVariable String ip) {
        int remaining = rateLimitService.remaining(ip);
        return ResponseEntity.ok(Map.of(
            "ip", ip,
            "remainingRequests", remaining
        ));
    }

    @GetMapping("/events")
    public ResponseEntity<List<String>> getEvents() {
        List<String> events = redisTemplate.opsForList().range(EVENTS_KEY, 0, 49);
        return ResponseEntity.ok(events != null ? events : List.of());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("status", "WAF is running"));
    }
}