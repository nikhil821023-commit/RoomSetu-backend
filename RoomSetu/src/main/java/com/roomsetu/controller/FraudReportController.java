package com.roomsetu.controller;

import com.roomsetu.dto.FraudReportRequest;
import com.roomsetu.dto.FraudReportResponse;
import com.roomsetu.entity.FraudReport;
import com.roomsetu.service.FraudReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class FraudReportController {

    private final FraudReportService fraudReportService;

    public FraudReportController(FraudReportService fraudReportService) {
        this.fraudReportService = fraudReportService;
    }

    @PostMapping
    public ResponseEntity<FraudReportResponse> submit(@AuthenticationPrincipal Long reporterId,
                                                      @Valid @RequestBody FraudReportRequest request) {
        FraudReport report = fraudReportService.submit(reporterId, request);
        return ResponseEntity.ok(FraudReportResponse.from(report));
    }

    // --- Admin endpoints: guard behind an admin role before deploying ---

    @GetMapping("/admin/queue")
    public ResponseEntity<List<FraudReportResponse>> queue() {
        List<FraudReportResponse> results = fraudReportService.openQueue()
                .stream().map(FraudReportResponse::from).toList();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/admin/{id}/resolve")
    public ResponseEntity<FraudReportResponse> resolve(@PathVariable Long id,
                                                       @RequestParam boolean resolved,
                                                       @RequestParam String reviewerName) {
        FraudReport report = fraudReportService.review(id, resolved, reviewerName);
        return ResponseEntity.ok(FraudReportResponse.from(report));
    }
}