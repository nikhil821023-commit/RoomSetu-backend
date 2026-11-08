package com.roomsetu.controller;

import com.roomsetu.dto.ListingInquiryRequest;
import com.roomsetu.dto.ListingInquiryResponse;
import com.roomsetu.entity.ListingInquiry;
import com.roomsetu.service.ListingAnalyticsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listings/{listingId}")
public class ListingAnalyticsController {

    private final ListingAnalyticsService listingAnalyticsService;

    public ListingAnalyticsController(ListingAnalyticsService listingAnalyticsService) {
        this.listingAnalyticsService = listingAnalyticsService;
    }

    // Called by the frontend whenever a seeker opens a listing's detail view.
    // viewerId is optional — allow anonymous/pre-login browsing to still count.
    @PostMapping("/view")
    public ResponseEntity<Void> recordView(@AuthenticationPrincipal Long viewerId,
                                           @PathVariable Long listingId) {
        listingAnalyticsService.recordView(listingId, viewerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inquire")
    public ResponseEntity<ListingInquiryResponse> inquire(@AuthenticationPrincipal Long inquirerId,
                                                          @PathVariable Long listingId,
                                                          @Valid @RequestBody ListingInquiryRequest request) {
        ListingInquiry inquiry = listingAnalyticsService.recordInquiry(listingId, inquirerId, request);
        return ResponseEntity.ok(ListingInquiryResponse.from(inquiry));
    }
}