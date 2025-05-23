package com.gaioz.stats.dto;

import com.gaioz.stats.model.Stat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetStatDto {
    private String statValue;
    private LocalDateTime updatedAt;

    public GetStatDto(Stat stat) {
        this.statValue = stat.getStatValue();
        this.updatedAt = stat.getUpdatedAt();
    }
}
