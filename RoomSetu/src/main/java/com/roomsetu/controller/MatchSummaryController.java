package com.roomsetu.controller;

import com.roomsetu.dto.MatchSummaryResponse;
import com.roomsetu.service.MatchSummaryService;
import com.roomsetu.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match-summary")
public class MatchSummaryController {

    private final MatchSummaryService matchSummaryService;
    private final ProfileService profileService;

    public MatchSummaryController(MatchSummaryService matchSummaryService, ProfileService profileService) {
        this.matchSummaryService = matchSummaryService;
        this.profileService = profileService;
    }

    @GetMapping("/{targetUserId}")
    public ResponseEntity<MatchSummaryResponse> summary(@AuthenticationPrincipal Long myUserId,
                                                        @PathVariable Long targetUserId) {
        var myProfile = profileService.getByUserId(myUserId);
        var targetProfile = profileService.getByUserId(targetUserId);
        return ResponseEntity.ok(matchSummaryService.summarize(myProfile, targetProfile));
    }
}