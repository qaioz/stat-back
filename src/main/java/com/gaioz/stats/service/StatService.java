package com.gaioz.stats.service;

import com.gaioz.stats.dto.GetStatResponse;
import com.gaioz.stats.dto.SetStatRequest;

public interface StatService {
    GetStatResponse getStat();
    void setStat(SetStatRequest setStatRequest);
}
