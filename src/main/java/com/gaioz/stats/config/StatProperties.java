package com.gaioz.stats.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Getter
@Component
public class StatProperties {

    @Getter
    @Value("${stat.cache-key}")
    private String cacheKey;

    @Value("${stat.cache-ttl-seconds}")
    private long cacheTtlSeconds;

    @Value("${stat.lock-ttl-seconds}")
    private long lockTtlSeconds;

    @Value("${stat.retry-ttl-seconds}")
    private long retryTtlSeconds;

    @Getter
    @Value("${stat.retry-interval-millis}")
    private long retryIntervalMillis;

    @Value("${stat.initial-value:0}")
    private String initialValue;

    public Duration getCacheTtl() {
        return Duration.ofSeconds(cacheTtlSeconds);
    }

    public Duration getLockTtl() {
        return Duration.ofSeconds(lockTtlSeconds);
    }

    public Duration getRetryTtl() {
        return Duration.ofSeconds(retryTtlSeconds);
    }
}
