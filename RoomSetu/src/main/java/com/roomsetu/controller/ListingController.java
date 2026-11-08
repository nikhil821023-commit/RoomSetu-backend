package com.roomsetu.controller;

import com.roomsetu.dto.ListingInquiryResponse;
import com.roomsetu.dto.ListingRequest;
import com.roomsetu.dto.ListingResponse;
import com.roomsetu.entity.Listing;
import com.roomsetu.repository.ListingRepository;
import com.roomsetu.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    public ResponseEntity<ListingResponse> create(@AuthenticationPrincipal Long hostId,
                                                  @Valid @RequestBody ListingRequest request) {
        Listing listing = listingService.create(hostId, request);
        return ResponseEntity.ok(ListingResponse.from(listing));
    }



    @GetMapping("/mine")
    public ResponseEntity<List<ListingResponse>> myListings(@AuthenticationPrincipal Long hostId) {
        List<ListingResponse> results = listingService.myListings(hostId)
                .stream().map(ListingResponse::from).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/public/by-host/{hostId}")
    public ResponseEntity<List<ListingResponse>> byHost(@PathVariable Long hostId) {
        List<ListingResponse> results = listingService.browseVerifiedByHost(hostId)
                .stream().map(ListingResponse::from).toList();
        return ResponseEntity.ok(results);
    }
    @GetMapping("/public/{listingId}")
    public ResponseEntity<ListingResponse> getOne(@PathVariable Long listingId) {
        Listing listing = listingService.getById(listingId);
        return ResponseEntity.ok(ListingResponse.from(listing));
    }
    @GetMapping("/admin/pending")
    public ResponseEntity<List<ListingResponse>> adminPendingQueue() {
        List<ListingResponse> results = listingService.pendingAndFlaggedQueue()
                .stream().map(ListingResponse::from).toList();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/admin/{listingId}/review")
    public ResponseEntity<ListingResponse> adminReview(@PathVariable Long listingId,
                                                       @RequestParam boolean approve) {
        Listing listing = listingService.adminReview(listingId, approve);
        return ResponseEntity.ok(ListingResponse.from(listing));
    }
    @GetMapping("/mine/inquiries")
    public ResponseEntity<List<ListingInquiryResponse>> myInquiries(@AuthenticationPrincipal Long hostId) {
        return ResponseEntity.ok(listingService.myInquiries(hostId));
    }

}