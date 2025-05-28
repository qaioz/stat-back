package com.gaioz.stats.service.impl;

import com.gaioz.stats.config.StatProperties;
import com.gaioz.stats.dto.FetchedFrom;
import com.gaioz.stats.dto.GetStatResponse;
import com.gaioz.stats.dto.SetStatRequest;
import com.gaioz.stats.exception.GetStatTimeoutException;
import com.gaioz.stats.exception.StatNotInitializedException;
import com.gaioz.stats.model.Stat;
import com.gaioz.stats.repository.StatRepository;
import com.gaioz.stats.service.RedisService;
import com.gaioz.stats.service.StatService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class StatServiceImpl implements StatService {


    private final StatRepository statRepository;
    private final RedisService redisService;
    private final StatProperties statProperties;

    @Autowired
    public StatServiceImpl(StatRepository statRepository,
                           RedisService redisService,
                           StatProperties statProperties) {
        this.statRepository = statRepository;
        this.redisService = redisService;
        this.statProperties = statProperties;
    }

    private void initStatIfNotExists() {
        if (statRepository.count() != 0) return;
        Stat stat = new Stat();
        stat.setStatValue(statProperties.getInitialValue());
        statRepository.save(stat);
    }

    //    post construct method to ensure stat is initialized
    @PostConstruct
    public void postConstruct() {
        log.info("Initializing StatServiceImpl");
        initStatIfNotExists();
        log.info("StatServiceImpl initialized successfully");
    }

    /**
     * Get stat from cache if available.
     * If not available in cache, it will try to get it from the database.
     * Meanwhile, to avoid stampede,  if another thread is trying to get the stat from the database,
     * this method will wait for the lock to be released and then return the cached value.
     */
    @Override
    public GetStatResponse getStat() {
        //retry every REETRY_INTERVAL milliseconds for up to RETRY_TTL seconds
        // every try: if the cache is available, return it,
        // else try to acquire a lock and get the stat from the database
        int retries = (int) (statProperties.getRetryTtlSeconds() * 1000 / statProperties.getRetryIntervalMillis());
        for (int i = 0; i < retries; i++) {
            Object cached = redisService.get(statProperties.getCacheKey());
            if (cached != null) {
                return getStatResponseFromCachedObjectElseThrowError(cached);
            }

            boolean lockAcquired = redisService.acquireLock(statProperties.getCacheKey(), statProperties.getLockTtl());
            if (lockAcquired) {
                try {
                    Optional<Stat> optionalStat = statRepository.findAny();
                    Stat stat = optionalStat.orElseThrow(() -> new StatNotInitializedException("Stat not initialized in the database."));
                    GetStatResponse freshDto = GetStatResponse.from(stat, FetchedFrom.DATABASE, statProperties.getCacheTtlSeconds());
                    redisService.set(statProperties.getCacheKey(), freshDto, statProperties.getCacheTtl());
                    return freshDto;
                } finally {
                    redisService.releaseLock(statProperties.getCacheKey());
                }
            }

            try {
                Thread.sleep(statProperties.getRetryIntervalMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting for lock", e);
            }
        }
        throw new GetStatTimeoutException("Failed to get stat after " + statProperties.getRetryTtlSeconds() + " seconds of retries.");
    }

    /**
     * If stat does not exist, it will be created with the value from SetStatRequest.
     * If it exists, it will be updated with the value from SetStatRequest.
     * After setting the stat, it will be cached in Redis.
     * <p>
     * Since we are using optimistic locking with @Version, StaleObjectStateException
     * may occur if multiple threads try to update the stat at the same time.
     */
    @Override
    @Transactional
    public void setStat(SetStatRequest setStatRequest) {
        Optional<Stat> optionalStat = statRepository.findAny();
        Stat stat;

        if (optionalStat.isEmpty()) {
            stat = Stat.fromDto(setStatRequest);
            statRepository.save(stat);
        } else {
            stat = optionalStat.get();
            stat.setStatValue(setStatRequest.getValue());
            statRepository.save(stat);
        }
        redisService.set(statProperties.getCacheKey(), GetStatResponse.from(stat), statProperties.getCacheTtl());
    }

    private GetStatResponse getStatResponseFromCachedObjectElseThrowError(Object cachedNonNull) {
        if (cachedNonNull instanceof GetStatResponse dto) {
            dto.setFetchedFrom(FetchedFrom.CACHE);
            Long ttl = redisService.getTtl(statProperties.getCacheKey());
            dto.setTtlSeconds(ttl == null ? 0 : ttl);
            return dto;
        } else {
            throw new IllegalStateException("Cached object is not of type GetStatResponse: " + cachedNonNull.getClass().getName());
        }
    }
}
