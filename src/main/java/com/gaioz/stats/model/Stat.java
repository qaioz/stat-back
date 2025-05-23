package com.gaioz.stats.model;

import com.gaioz.stats.dto.SetStatDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stats")
@Getter
@Setter
@RequiredArgsConstructor
public class Stat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "stat_value", nullable = false)
    private String statValue;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Stat fromDto(SetStatDto dto) {
        Stat stat = new Stat();
        stat.setStatValue(dto.getValue());
        return stat;
    }
}
