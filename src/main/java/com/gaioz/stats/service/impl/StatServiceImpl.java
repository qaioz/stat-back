package com.gaioz.stats.service.impl;

import com.gaioz.stats.dto.FetchedFrom;
import com.gaioz.stats.dto.GetStatResponse;
import com.gaioz.stats.dto.SetStatRequest;
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

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class StatServiceImpl implements StatService {

    private static final String STAT_CACHE_KEY = "stat";
    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    private static final Duration LOCK_TTL = Duration.ofSeconds(60);

    private final StatRepository statRepository;
    private final RedisService redisService;

    @Autowired
    public StatServiceImpl(StatRepository statRepository, RedisService redisService) {
        this.statRepository = statRepository;
        this.redisService = redisService;
    }

    private void initStatIfNotExists() {
        if (statRepository.count() != 0) return;
        Stat stat = new Stat();
        stat.setStatValue("0");
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
        Object cached = redisService.get(STAT_CACHE_KEY);

        if (cached != null) {
            return getStatResponseFromCachedObjectElseThrowError(cached);
        }

        return redisService.tryWithLockAndWait(STAT_CACHE_KEY, LOCK_TTL, 100, 300, () -> {
            Optional<Stat> optionalStat = statRepository.findAny();
            Stat stat = optionalStat.orElseThrow(() -> new StatNotInitializedException("Stat not initialized in the database."));
            GetStatResponse freshDto = GetStatResponse.from(stat, FetchedFrom.DATABASE, CACHE_TTL.getSeconds());
            redisService.set(STAT_CACHE_KEY, freshDto, CACHE_TTL);
            return freshDto;
        }, () -> {
            Object retryCached = redisService.get(STAT_CACHE_KEY);
            if (retryCached == null) return null;
            return getStatResponseFromCachedObjectElseThrowError(retryCached);
        });
    }

    /**
     * If stat does not exist, it will be created with the value from SetStatRequest.
     * If it exists, it will be updated with the value from SetStatRequest.
     * After setting the stat, it will be cached in Redis.
     *
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
        redisService.set(STAT_CACHE_KEY, GetStatResponse.from(stat), CACHE_TTL);
    }

    private GetStatResponse getStatResponseFromCachedObjectElseThrowError(Object cachedNonNull) {
        if (cachedNonNull instanceof GetStatResponse dto) {
            dto.setFetchedFrom(FetchedFrom.CACHE);
            Long ttl = redisService.getTtl(STAT_CACHE_KEY);
            dto.setTtlSeconds(ttl == null ? 0 : ttl);
            return dto;
        } else {
            throw new IllegalStateException("Cached object is not of type GetStatResponse: " + cachedNonNull.getClass().getName());
        }
    }
}
