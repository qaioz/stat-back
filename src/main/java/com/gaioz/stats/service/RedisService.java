package com.gaioz.stats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.lock-prefix:lock:}")
    private String LOCK_PREFIX;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Long getTtl(String key) {
        return redisTemplate.getExpire(key);
    }

    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public boolean acquireLock(String key, Duration duration) {
        String lockKey = LOCK_PREFIX + key;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", duration);
        return Boolean.TRUE.equals(success);
    }

    public void releaseLock(String key) {
        redisTemplate.delete(LOCK_PREFIX + key);
    }
}
