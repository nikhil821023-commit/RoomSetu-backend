package com.roomsetu.controller;

import com.roomsetu.dto.DiscoveryCardResponse;
import com.roomsetu.dto.MatchResponse;
import com.roomsetu.entity.Match;
import com.roomsetu.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }
    @GetMapping("/discover")
    public ResponseEntity<List<DiscoveryCardResponse>> discover(@AuthenticationPrincipal Long userId,
                                                                @RequestParam(defaultValue = "10") double radiusKm) {
        return ResponseEntity.ok(matchService.discover(userId, radiusKm));
    }



    @PostMapping("/matches/request/{targetUserId}")
    public ResponseEntity<MatchResponse> requestMatch(@AuthenticationPrincipal Long requesterId,
                                                      @PathVariable Long targetUserId) {
        Match match = matchService.requestMatch(requesterId, targetUserId);
        return ResponseEntity.ok(MatchResponse.from(match));
    }

    @PostMapping("/matches/{matchId}/accept")
    public ResponseEntity<MatchResponse> accept(@AuthenticationPrincipal Long userId,
                                                @PathVariable Long matchId) {
        Match match = matchService.respond(matchId, userId, true);
        return ResponseEntity.ok(MatchResponse.from(match));
    }

    @PostMapping("/matches/{matchId}/reject")
    public ResponseEntity<MatchResponse> reject(@AuthenticationPrincipal Long userId,
                                                @PathVariable Long matchId) {
        Match match = matchService.respond(matchId, userId, false);
        return ResponseEntity.ok(MatchResponse.from(match));
    }

    @GetMapping("/matches/mine")
    public ResponseEntity<List<MatchResponse>> myMatches(@AuthenticationPrincipal Long userId) {
        List<MatchResponse> results = matchService.myMatches(userId)
                .stream().map(MatchResponse::from).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/matches/incoming")
    public ResponseEntity<List<MatchResponse>> incoming(@AuthenticationPrincipal Long userId) {
        List<MatchResponse> results = matchService.incomingRequests(userId)
                .stream().map(MatchResponse::from).toList();
        return ResponseEntity.ok(results);
    }
}