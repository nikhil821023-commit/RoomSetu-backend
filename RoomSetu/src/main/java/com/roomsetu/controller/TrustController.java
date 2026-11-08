package com.roomsetu.controller;

import com.roomsetu.dto.TrustScoreResponse;
import com.roomsetu.service.TrustScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trust")
public class TrustController {

    private final TrustScoreService trustScoreService;

    public TrustController(TrustScoreService trustScoreService) {
        this.trustScoreService = trustScoreService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<TrustScoreResponse> get(@PathVariable Long userId) {
        return ResponseEntity.ok(trustScoreService.compute(userId));
    }
}