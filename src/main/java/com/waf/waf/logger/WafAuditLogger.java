package com.waf.waf.logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.WafResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WafAuditLogger {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EVENTS_KEY = "waf:events";
    private static final int MAX_EVENTS = 100;

    public void log(ExtractRequestDto request, WafResult result) {
        if (result.isBlocked()) {
            log.warn("[WAF-BLOCKED] ip={} method={} path={} query={} payloadLength={} reason={} status={} fromCache={}",
                request.getIp(), request.getMethod(), request.getPath(),
                request.getQuery(), request.getPayloadLength(),
                result.getErrorMessage(), result.getStatusCode(), result.isFromCache()
            );
            pushEvent(request, result);
        } else {
            log.info("[WAF-ALLOWED] ip={} method={} path={} query={} payloadLength={}",
                request.getIp(), request.getMethod(), request.getPath(),
                request.getQuery(), request.getPayloadLength()
            );
        }
    }

    public void logEarlyReject(String ip, int statusCode, String reason) {
        log.warn("[WAF-BLOCKED] ip={} reason={} status={} stage=pre-normalization",
            ip, reason, statusCode);
        pushEarlyEvent(ip, statusCode, reason);
    }

    private void pushEvent(ExtractRequestDto request, WafResult result) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("timestamp", Instant.now().toString());
            event.put("ip", request.getIp());
            event.put("method", request.getMethod());
            event.put("path", request.getPath());
            event.put("query", request.getQuery());
            event.put("reason", result.getErrorMessage());
            event.put("status", result.getStatusCode());

            String json = objectMapper.writeValueAsString(event);
            redisTemplate.opsForList().leftPush(EVENTS_KEY, json);
            redisTemplate.opsForList().trim(EVENTS_KEY, 0, MAX_EVENTS - 1);
        } catch (Exception e) {
            log.error("Failed to push WAF event to Redis: {}", e.getMessage());
        }
    }

    private void pushEarlyEvent(String ip, int statusCode, String reason) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("timestamp", Instant.now().toString());
            event.put("ip", ip);
            event.put("method", "?");
            event.put("path", "?");
            event.put("query", null);
            event.put("reason", reason);
            event.put("status", statusCode);

            String json = objectMapper.writeValueAsString(event);
            redisTemplate.opsForList().leftPush(EVENTS_KEY, json);
            redisTemplate.opsForList().trim(EVENTS_KEY, 0, MAX_EVENTS - 1);
        } catch (Exception e) {
            log.error("Failed to push early WAF event to Redis: {}", e.getMessage());
        }
    }
}