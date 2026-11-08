package com.roomsetu.controller;

import com.roomsetu.dto.EmailCodeConfirmRequest;
import com.roomsetu.dto.EmailVerificationRequest;
import com.roomsetu.service.WorkEmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/verification/email")
public class EmailVerificationController {

    private final WorkEmailVerificationService service;

    public EmailVerificationController(WorkEmailVerificationService service) {
        this.service = service;
    }

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> request(@AuthenticationPrincipal Long userId,
                                                       @Valid @RequestBody EmailVerificationRequest req) {
        service.requestCode(userId, req.getEmail());
        return ResponseEntity.ok(Map.of("message", "Code sent"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirm(@AuthenticationPrincipal Long userId,
                                                       @Valid @RequestBody EmailCodeConfirmRequest req) {
        String domain = service.confirmCode(userId, req.getCode());
        return ResponseEntity.ok(Map.of("domain", domain));
    }
}