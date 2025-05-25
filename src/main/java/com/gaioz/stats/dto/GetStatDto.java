package com.gaioz.stats.dto;

import com.gaioz.stats.model.Stat;
import lombok.*;

@Getter
@Setter
public class GetStatDto {
    private String statValue;
    private Long version;

    public GetStatDto(Stat stat) {
        this.statValue = stat.getStatValue();
        this.version = stat.getVersion();
    }
}
