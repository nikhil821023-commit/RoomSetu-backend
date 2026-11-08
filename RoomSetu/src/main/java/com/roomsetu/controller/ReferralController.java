package com.roomsetu.controller;

import com.roomsetu.dto.ReferralCodeResponse;
import com.roomsetu.dto.ReferralStatsResponse;
import com.roomsetu.service.ReferralService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/referrals")
public class ReferralController {

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @GetMapping("/my-code")
    public ResponseEntity<ReferralCodeResponse> myCode(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(new ReferralCodeResponse(referralService.getOrCreateCode(userId)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ReferralStatsResponse> stats(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(referralService.statsFor(userId));
    }
}