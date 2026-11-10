package com.roomsetu.controller;

import com.roomsetu.dto.auth.AuthResponse;
import com.roomsetu.dto.auth.EmailOtpRequest;
import com.roomsetu.dto.auth.EmailOtpVerifyRequest;
import com.roomsetu.dto.auth.OtpRequest;
import com.roomsetu.dto.auth.OtpVerifyRequest;
import com.roomsetu.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/otp/request")
    public ResponseEntity<String> requestOtp(@Valid @RequestBody OtpRequest request, HttpServletRequest http) {
        authService.requestOtp(request.getPhone(), http.getRemoteAddr());
        return ResponseEntity.ok("OTP sent");
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtpAndLogin(request));
    }

    @PostMapping("/email/request")
    public ResponseEntity<String> requestEmailOtp(@Valid @RequestBody EmailOtpRequest request, HttpServletRequest http) {
        authService.requestEmailOtp(request.getEmail(), http.getRemoteAddr());
        return ResponseEntity.ok("Code sent");
    }

    @PostMapping("/email/verify")
    public ResponseEntity<AuthResponse> verifyEmailOtp(@Valid @RequestBody EmailOtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyEmailOtpAndLogin(request));
    }
}