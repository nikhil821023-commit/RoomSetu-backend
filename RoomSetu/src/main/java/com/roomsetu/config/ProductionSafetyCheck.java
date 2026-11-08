package com.roomsetu.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionSafetyCheck {

    @Value("${roomsetu.jwt.secret}")
    private String jwtSecret;

    @Value("${roomsetu.otp.dev-numbers:}")
    private String devNumbers;

    @Value("${roomsetu.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${roomsetu.otp.console-fallback:true}")
    private boolean consoleFallback;

    @PostConstruct
    void check() {
        if (jwtSecret == null || jwtSecret.length() < 32 || jwtSecret.startsWith("replace-this")) {
            throw new IllegalStateException(
                    "JWT_SECRET is missing or still the default. Set a long random value before running in prod.");
        }
        if (!devNumbers.isBlank()) {
            System.err.println("[SAFETY] OTP_DEV_NUMBERS is set: those numbers skip SMS. Fine for a private demo only.");
        }
        if (!smsEnabled && consoleFallback) {
            System.err.println("[SAFETY] SMS is off: login codes print in the server logs, so only you can complete a login.");
        }
        if (!smsEnabled && !consoleFallback) {
            System.err.println("[SAFETY] SMS is off and the console fallback is off: nobody can log in.");
        }
    }
}