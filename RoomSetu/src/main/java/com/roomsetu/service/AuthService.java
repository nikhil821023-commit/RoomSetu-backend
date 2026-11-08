package com.roomsetu.service;

import com.roomsetu.dto.auth.AuthResponse;
import com.roomsetu.dto.auth.OtpVerifyRequest;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.Role;
import com.roomsetu.entity.enums.VerificationStatus;
import com.roomsetu.repository.UserRepository;
import com.roomsetu.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, OtpService otpService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }

    public void requestOtp(String phone, String clientIp) {
        otpService.generateAndSend(phone, clientIp);
    }


    public AuthResponse verifyOtpAndLogin(OtpVerifyRequest request) {
        if (!otpService.verify(request.getPhone(), request.getOtp())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> registerNewUser(request));

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getRole().name(),
                user.getVerificationStatus().name(),
                user.isAdmin()
        );
    }

    private User registerNewUser(OtpVerifyRequest request) {
        if (request.getName() == null || request.getRole() == null) {
            throw new IllegalArgumentException("Name and role are required for first-time signup");
        }
        User user = User.builder()
                .phone(request.getPhone())
                .name(request.getName())
                .role(request.getRole())
                .verificationStatus(VerificationStatus.UNVERIFIED)
                .build();
        return userRepository.save(user);
    }
}