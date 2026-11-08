package com.roomsetu.controller;

import com.roomsetu.dto.HostDashboardSummaryResponse;
import com.roomsetu.dto.ListingResponse;
import com.roomsetu.service.HostDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/host-dashboard")
public class HostDashboardController {

    private final HostDashboardService hostDashboardService;

    public HostDashboardController(HostDashboardService hostDashboardService) {
        this.hostDashboardService = hostDashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<HostDashboardSummaryResponse> summary(@AuthenticationPrincipal Long hostId) {
        return ResponseEntity.ok(hostDashboardService.summaryFor(hostId));
    }

    @PostMapping("/listings/{listingId}/filled")
    public ResponseEntity<ListingResponse> markFilled(@AuthenticationPrincipal Long hostId,
                                                      @PathVariable Long listingId,
                                                      @RequestParam boolean filled) {
        var listing = hostDashboardService.setFilled(listingId, hostId, filled);
        return ResponseEntity.ok(ListingResponse.from(listing));
    }
}