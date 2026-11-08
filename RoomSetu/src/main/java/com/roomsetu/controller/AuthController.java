package com.roomsetu.controller;

import com.roomsetu.dto.auth.AuthResponse;
import com.roomsetu.dto.auth.OtpRequest;
import com.roomsetu.dto.auth.OtpVerifyRequest;
import com.roomsetu.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;


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
}