package com.gaioz.stats.controller;


import com.gaioz.stats.dto.GetStatResponse;
import com.gaioz.stats.dto.SetStatRequest;
import com.gaioz.stats.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("api/stats")
public class StatController {
    private final StatService statService;

    @Autowired
    public StatController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/get")
    public ResponseEntity<GetStatResponse> getStat() {
        return ResponseEntity.ok(statService.getStat());
    }

    @PutMapping("/set")
    public ResponseEntity<Void> setStat(@RequestBody SetStatRequest setStatRequest) {
        statService.setStat(setStatRequest);
        return ResponseEntity.noContent().build();
    }
}
