package com.roomsetu.controller;

import com.roomsetu.dto.ListingVerificationRequest;
import com.roomsetu.entity.ListingVerification;
import com.roomsetu.service.ListingVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings/{listingId}/verification")
public class ListingVerificationController {

    private final ListingVerificationService verificationService;

    public ListingVerificationController(ListingVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/submit")
    public ResponseEntity<ListingVerification> submit(@AuthenticationPrincipal Long hostId,
                                                      @PathVariable Long listingId,
                                                      @Valid @RequestBody ListingVerificationRequest request) {
        ListingVerification v = verificationService.submit(
                listingId, hostId, request.getDocType(), request.getDocUrl());
        return ResponseEntity.ok(v);
    }

    // --- Admin endpoints: guard behind an admin role before deploying ---

    @GetMapping("/admin/pending")
    public ResponseEntity<List<ListingVerification>> pendingQueue() {
        return ResponseEntity.ok(verificationService.pendingQueue());
    }

    @PostMapping("/admin/{verificationId}/review")
    public ResponseEntity<ListingVerification> review(@PathVariable Long verificationId,
                                                      @RequestParam boolean approve,
                                                      @RequestParam String reviewerName) {
        return ResponseEntity.ok(verificationService.review(verificationId, approve, reviewerName));
    }
}