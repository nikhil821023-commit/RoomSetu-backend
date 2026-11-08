package com.roomsetu.controller;

import com.roomsetu.dto.FirstMessageSuggestionResponse;
import com.roomsetu.service.FirstMessageAssistantService;
import com.roomsetu.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/first-message")
public class FirstMessageController {

    private final FirstMessageAssistantService firstMessageAssistantService;
    private final ProfileService profileService;

    public FirstMessageController(FirstMessageAssistantService firstMessageAssistantService,
                                  ProfileService profileService) {
        this.firstMessageAssistantService = firstMessageAssistantService;
        this.profileService = profileService;
    }

    @GetMapping("/{targetUserId}")
    public ResponseEntity<FirstMessageSuggestionResponse> suggest(@AuthenticationPrincipal Long myUserId,
                                                                  @PathVariable Long targetUserId) {
        var myProfile = profileService.getByUserId(myUserId);
        var targetProfile = profileService.getByUserId(targetUserId);
        return ResponseEntity.ok(firstMessageAssistantService.suggest(myProfile, targetProfile));
    }
}