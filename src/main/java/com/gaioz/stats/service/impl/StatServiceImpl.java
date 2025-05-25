package com.gaioz.stats.service.impl;

import com.gaioz.stats.dto.GetStatDto;
import com.gaioz.stats.dto.SetStatDto;
import com.gaioz.stats.model.Stat;
import com.gaioz.stats.repository.StatRepository;
import com.gaioz.stats.service.StatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;

    @Autowired
    public StatServiceImpl(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    public void initStatIfNotExists() {
        if(statRepository.count() != 0) return;
        Stat stat = new Stat();
        stat.setStatValue("0");
        statRepository.save(stat);
    }

    @Override
    public GetStatDto getStat() {
        initStatIfNotExists();
        List<Stat> all = statRepository.findAll();
        return new GetStatDto(all.get(0));
    }

    @Override
    @Transactional
    public boolean setStat(SetStatDto setStatDto) {
        List<Stat> all = statRepository.findAll();
        if (all.isEmpty()) {
            Stat stat = Stat.fromDto(setStatDto);
            statRepository.save(stat);
            return true;
        } else {
            Stat existingStat = all.get(0);
            boolean stateChanged = !existingStat.getStatValue().equals(setStatDto.getValue());
            existingStat.setStatValue(setStatDto.getValue());
            statRepository.save(existingStat);
            return stateChanged;
        }
    }
}
