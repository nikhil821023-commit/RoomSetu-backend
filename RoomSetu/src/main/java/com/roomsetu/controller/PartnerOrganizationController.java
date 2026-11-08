package com.roomsetu.controller;

import com.roomsetu.dto.PartnerOrganizationRequest;
import com.roomsetu.dto.PartnerOrganizationResponse;
import com.roomsetu.entity.PartnerOrganization;
import com.roomsetu.service.PartnerOrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partner-organizations")
public class PartnerOrganizationController {

    private final PartnerOrganizationService partnerOrganizationService;

    public PartnerOrganizationController(PartnerOrganizationService partnerOrganizationService) {
        this.partnerOrganizationService = partnerOrganizationService;
    }

    // Admin-only — same guard requirement as every other admin endpoint so far
    @PostMapping("/admin")
    public ResponseEntity<PartnerOrganizationResponse> create(@Valid @RequestBody PartnerOrganizationRequest request) {
        PartnerOrganization org = partnerOrganizationService.create(request);
        long memberCount = partnerOrganizationService.memberCount(org.getId());
        return ResponseEntity.ok(PartnerOrganizationResponse.from(org, memberCount));
    }
}