package com.roomsetu.service;

import com.roomsetu.dto.auth.AuthResponse;
import com.roomsetu.dto.auth.EmailOtpVerifyRequest;
import com.roomsetu.dto.auth.OtpVerifyRequest;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.Role;
import com.roomsetu.repository.UserRepository;
import com.roomsetu.security.JwtUtil;
import com.roomsetu.util.EmailDomains;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, OtpService otpService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }

    // ---------- phone ----------

    public void requestOtp(String phone, String clientIp) {
        otpService.generateAndSend(phone, clientIp);
    }

    public AuthResponse verifyOtpAndLogin(OtpVerifyRequest request) {
        if (!otpService.verify(request.getPhone(), request.getOtp())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    requireSignupDetails(request.getName(), request.getRole());
                    return userRepository.save(User.builder()
                            .phone(request.getPhone())
                            .name(request.getName().trim())
                            .role(request.getRole())
                            .build());
                });
        return toResponse(user);
    }

    // ---------- email ----------

    public void requestEmailOtp(String rawEmail, String clientIp) {
        String typed = rawEmail.trim().toLowerCase();
        if (typed.length() > 150 || !EMAIL.matcher(typed).matches()) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        String canonical = EmailDomains.canonical(typed);
        if (canonical.startsWith("@")) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        if (EmailDomains.isDisposable(EmailDomains.domainOf(canonical))) {
            throw new IllegalArgumentException("Disposable email addresses aren't allowed. Use your regular email");
        }
        otpService.generateAndSendEmail(canonical, typed, clientIp);
    }

    public AuthResponse verifyEmailOtpAndLogin(EmailOtpVerifyRequest request) {
        String canonical = EmailDomains.canonical(request.getEmail());
        if (!otpService.verifyEmail(canonical, request.getOtp())) {
            throw new IllegalArgumentException("Invalid or expired code");
        }
        User user = userRepository.findByEmail(canonical)
                .orElseGet(() -> {
                    requireSignupDetails(request.getName(), request.getRole());
                    return userRepository.save(User.builder()
                            .email(canonical)
                            .name(request.getName().trim())
                            .role(request.getRole())
                            .build());
                });
        return toResponse(user);
    }

    // ---------- shared ----------

    private void requireSignupDetails(String name, Role role) {
        if (name == null || name.isBlank() || role == null) {
            throw new IllegalArgumentException(
                    "No account found. Go back and tap \"New here? Create an account\"");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Name is too long");
        }
        if (role != Role.SEEKER && role != Role.HOST) {
            throw new IllegalArgumentException("Pick Seeker or Host");
        }
    }

    private AuthResponse toResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), Objects.toString(user.getPhone(), ""));
        return new AuthResponse(token, user.getId(), user.getName(),
                user.getRole().name(), user.getVerificationStatus().name());
    }
}