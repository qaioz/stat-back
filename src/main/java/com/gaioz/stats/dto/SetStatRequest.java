package com.gaioz.stats.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetStatRequest {
    private String value;
}
