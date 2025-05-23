package com.gaioz.stats.service;

import com.gaioz.stats.dto.GetStatDto;
import com.gaioz.stats.dto.SetStatDto;

public interface StatService {
    GetStatDto getStat();
    boolean setStat(SetStatDto setStatDto);
}
