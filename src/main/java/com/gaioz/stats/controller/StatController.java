package com.gaioz.stats.controller;


import com.gaioz.stats.dto.GetStatDto;
import com.gaioz.stats.dto.SetStatDto;
import com.gaioz.stats.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("api/stats")
public class StatController {
    private final StatService statService;

    @Autowired
    public StatController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/get")
    public ResponseEntity<GetStatDto> getStat() {
        return ResponseEntity.ok(statService.getStat());
    }

    @PutMapping("/set")
    public ResponseEntity<Boolean> setStat(SetStatDto setStatDto) {
        boolean stateChanged = statService.setStat(setStatDto);
        return stateChanged ? ResponseEntity.ok(Boolean.TRUE) : ResponseEntity.ok(Boolean.FALSE);
    }
}
