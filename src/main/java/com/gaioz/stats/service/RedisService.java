package com.gaioz.stats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "lock:";

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

    /**
     * @param key
     * @param lockTtl
     * @param maxRetries
     * @param delayMillis
     * @param task
     * @param fallbackIfCacheAppears
     * @return
     * @param <T>
     */
    public <T> T tryWithLockAndWait(
            String key,
            Duration lockTtl,
            int maxRetries,
            int delayMillis,
            Supplier<T> task,
            Supplier<T> fallbackIfCacheAppears
    ) {
        for (int i = 0; i < maxRetries; i++) {
            boolean lockAcquired = acquireLock(key, lockTtl);
            if (lockAcquired) {
                try {
                    return task.get();
                } finally {
                    releaseLock(key);
                }
            } else {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted while waiting for Redis lock", e);
                }

                T cached = fallbackIfCacheAppears.get();
                if (cached != null) {
                    return cached;
                }
            }
        }

        throw new RuntimeException("Failed to acquire Redis lock after waiting");
    }


    public void releaseLock(String key) {
        redisTemplate.delete(LOCK_PREFIX + key);
    }
}
