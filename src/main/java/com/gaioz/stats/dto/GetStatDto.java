package com.gaioz.stats.dto;

import com.gaioz.stats.model.Stat;
import jakarta.persistence.Transient;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
public class GetStatDto implements Serializable {
    private String statValue;
    private Long version;

    // This model is also used in redis cache
    // But this field should not be saved in redis cache
    @Transient
    private FetchedFrom fetchedFrom;

    @Transient
    private Long ttlSeconds;

    private GetStatDto(Stat stat) {
        this.statValue = stat.getStatValue();
        this.version = stat.getVersion();
    }

    public static GetStatDto from(Stat stat) {
        return new GetStatDto(stat);
    }

    public static GetStatDto from(Stat stat, FetchedFrom fetchedFrom, Long ttlSeconds) {
        GetStatDto dto = new GetStatDto(stat);
        dto.setFetchedFrom(fetchedFrom);
        dto.setTtlSeconds(ttlSeconds);
        return dto;
    }
}
