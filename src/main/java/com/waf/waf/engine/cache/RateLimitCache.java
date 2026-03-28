package com.waf.waf.engine.cache;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitCache {
    private final StringRedisTemplate redisTemplate;

    private static final String RATE_PREFIX="rate:";

    private String key(String ip){
        return RATE_PREFIX+ip;
    }

    public int increment(String ip,Duration window){
        String key=key(ip);
        Long count=redisTemplate.opsForValue().increment(key);
        if(count!=null && count==1){
            redisTemplate.expire(key, window);
        }
        return count!=null?count.intValue():0;
    }

    public int get(String ip){
        String val=redisTemplate.opsForValue().get(key(ip));
        return val!=null?Integer.parseInt(val):0;
    }

   
}
