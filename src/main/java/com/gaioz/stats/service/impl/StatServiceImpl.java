package com.gaioz.stats.service.impl;

import com.gaioz.stats.dto.FetchedFrom;
import com.gaioz.stats.dto.GetStatDto;
import com.gaioz.stats.dto.SetStatDto;
import com.gaioz.stats.model.Stat;
import com.gaioz.stats.repository.StatRepository;
import com.gaioz.stats.service.RedisService;
import com.gaioz.stats.service.StatService;
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
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

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


    @Override
    public GetStatDto getStat() {
        Object cached = redisService.get(STAT_CACHE_KEY);

        if (cached instanceof GetStatDto dto) {
            dto.setFetchedFrom(FetchedFrom.CACHE);
            Long ttl = redisService.getTtl(STAT_CACHE_KEY);
            dto.setTtlSeconds(ttl == null ? 0 : ttl);
            return dto;
        }

        return redisService.tryWithLockAndWait(
                STAT_CACHE_KEY,
                LOCK_TTL,
                10,
                100,
                () -> {
                    initStatIfNotExists();
                    Optional<Stat> optionalStat = statRepository.findAny();
                    Stat stat = optionalStat.orElseThrow(() -> new IllegalStateException("No stat found"));
                    GetStatDto freshDto = GetStatDto.from(stat, FetchedFrom.DATABASE, CACHE_TTL.getSeconds());
                    redisService.set(STAT_CACHE_KEY, freshDto, CACHE_TTL);
                    return freshDto;
                },
                () -> {
                    Object retryCached = redisService.get(STAT_CACHE_KEY);
                    return retryCached instanceof GetStatDto dto ? dto : null;
                }
        );
    }

    @Override
    @Transactional
    public boolean setStat(SetStatDto setStatDto) {
        Optional<Stat> optionalStat = statRepository.findAny();
        Stat stat;
        boolean stateChanged;

        if (optionalStat.isEmpty()) {
            stat = Stat.fromDto(setStatDto);
            statRepository.save(stat);
            stateChanged = true;
        } else {
            stat = optionalStat.get();
            stateChanged = !stat.getStatValue().equals(setStatDto.getValue());
            stat.setStatValue(setStatDto.getValue());
            statRepository.save(stat);
        }

        redisService.set(STAT_CACHE_KEY, GetStatDto.from(stat), CACHE_TTL);

        return stateChanged;
    }
}
