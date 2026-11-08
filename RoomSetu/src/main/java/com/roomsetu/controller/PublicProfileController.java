package com.roomsetu.controller;

import com.roomsetu.dto.PublicProfileResponse;
import com.roomsetu.service.PublicProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public-profile")
public class PublicProfileController {

    private final PublicProfileService publicProfileService;

    public PublicProfileController(PublicProfileService publicProfileService) {
        this.publicProfileService = publicProfileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PublicProfileResponse> get(@AuthenticationPrincipal Long viewerId,
                                                     @PathVariable Long userId) {
        return ResponseEntity.ok(publicProfileService.get(viewerId, userId));
    }
}