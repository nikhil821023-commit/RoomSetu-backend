package com.roomsetu.controller;

import com.roomsetu.dto.ListingQualityReportResponse;
import com.roomsetu.entity.Listing;
import com.roomsetu.repository.ListingRepository;
import com.roomsetu.service.ListingQualityCoachService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listings/{listingId}/quality")
public class ListingQualityController {

    private final ListingQualityCoachService listingQualityCoachService;
    private final ListingRepository listingRepository;

    public ListingQualityController(ListingQualityCoachService listingQualityCoachService,
                                    ListingRepository listingRepository) {
        this.listingQualityCoachService = listingQualityCoachService;
        this.listingRepository = listingRepository;
    }

    @GetMapping
    public ResponseEntity<ListingQualityReportResponse> assess(@AuthenticationPrincipal Long hostId,
                                                               @PathVariable Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (!listing.getHost().getId().equals(hostId)) {
            throw new IllegalArgumentException("Only the listing's host can view its quality report");
        }

        return ResponseEntity.ok(listingQualityCoachService.assess(listing));
    }
}