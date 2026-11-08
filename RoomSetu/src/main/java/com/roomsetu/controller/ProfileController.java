package com.roomsetu.controller;

import com.roomsetu.dto.ProfileRequest;
import com.roomsetu.entity.Profile;
import com.roomsetu.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping
    public ResponseEntity<Profile> upsertProfile(@AuthenticationPrincipal Long userId,
                                                 @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.upsert(userId, request));
    }

    @GetMapping("/me")
    public ResponseEntity<Profile> getMyProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(profileService.getByUserId(userId));
    }
}