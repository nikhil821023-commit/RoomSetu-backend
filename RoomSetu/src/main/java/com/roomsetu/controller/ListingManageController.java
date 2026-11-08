package com.roomsetu.controller;

import com.roomsetu.dto.ListingResponse;
import com.roomsetu.dto.ListingUpdateRequest;
import com.roomsetu.service.ListingManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listings")
public class ListingManageController {

    private final ListingManagementService managementService;

    public ListingManageController(ListingManagementService managementService) {
        this.managementService = managementService;
    }

    @PutMapping("/{listingId}")
    public ResponseEntity<ListingResponse> update(@AuthenticationPrincipal Long hostId,
                                                  @PathVariable Long listingId,
                                                  @Valid @RequestBody ListingUpdateRequest request) {
        return ResponseEntity.ok(ListingResponse.from(managementService.update(hostId, listingId, request)));
    }

    @PostMapping("/{listingId}/filled")
    public ResponseEntity<ListingResponse> setFilled(@AuthenticationPrincipal Long hostId,
                                                     @PathVariable Long listingId,
                                                     @RequestParam boolean filled) {
        return ResponseEntity.ok(ListingResponse.from(managementService.setFilled(hostId, listingId, filled)));
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long hostId, @PathVariable Long listingId) {
        managementService.delete(hostId, listingId);
        return ResponseEntity.noContent().build();
    }
}