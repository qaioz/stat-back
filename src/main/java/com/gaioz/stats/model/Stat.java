package com.gaioz.stats.model;

import com.gaioz.stats.dto.SetStatRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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

    @Version
    private Long version;

    public static Stat fromDto(SetStatRequest dto) {
        Stat stat = new Stat();
        stat.setStatValue(dto.getValue());
        return stat;
    }


}
