package com.roomsetu.service;

import com.roomsetu.sms.SmsGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Codes and rate-limit windows live in memory: lost on restart and not shared across
 * servers. Fine for one instance; move to Redis before running more than one.
 */
@Service
public class OtpService {

    private static final int RESEND_COOLDOWN_SECONDS = 30;
    private static final int MAX_SENDS_PER_PHONE_PER_HOUR = 5;
    private static final int MAX_SENDS_PER_IP_PER_HOUR = 15;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    @Value("${roomsetu.otp.expiry-seconds}")
    private long expirySeconds;

    @Value("${roomsetu.otp.console-fallback:true}")
    private boolean consoleFallback;

    @Value("${roomsetu.otp.dev-numbers:}")
    private String devNumbersCsv;

    private record OtpEntry(String code, Instant expiresAt, Instant sentAt, int attempts) {}

    private final SmsGateway smsGateway;
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> phoneSends = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> ipSends = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public OtpService(SmsGateway smsGateway) {
        this.smsGateway = smsGateway;
    }


    private boolean isDevNumber(String phone) {
        if (devNumbersCsv == null || devNumbersCsv.isBlank()) return false;
        return Arrays.stream(devNumbersCsv.split(",")).map(String::trim).anyMatch(phone::equals);
    }

    public void generateAndSend(String phone, String clientIp) {
        Instant now = Instant.now();

        OtpEntry existing = store.get(phone);
        if (existing != null && now.isBefore(existing.sentAt().plusSeconds(RESEND_COOLDOWN_SECONDS))) {
            throw new IllegalArgumentException("Please wait 30 seconds before requesting another code");
        }
        checkAndRecord(phoneSends, phone, MAX_SENDS_PER_PHONE_PER_HOUR,
                "Too many codes requested for this number. Try again in an hour");
        checkAndRecord(ipSends, clientIp, MAX_SENDS_PER_IP_PER_HOUR,
                "Too many requests from your network. Try again later");

        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(phone, new OtpEntry(code, now.plusSeconds(expirySeconds), now, 0));

        if (isDevNumber(phone)) {
            // Free path for test accounts: no SMS is sent, the code is printed to the server console.
            System.out.printf("[OTP] (dev number) %s for %s%n", code, phone);
            return;
        }

        boolean delivered = smsGateway.sendOtp(phone, code);

        if (!delivered) {
            if (consoleFallback) {
                // Dev only. Set OTP_CONSOLE_FALLBACK=false so a failed SMS shows as an error instead.
                System.out.printf("[OTP] (console fallback) %s for %s%n", code, phone);
            } else {
                store.remove(phone);
                throw new IllegalArgumentException("We couldn't send the SMS right now. Please try again in a moment");
            }
        }
    }

    public boolean verify(String phone, String code) {
        OtpEntry entry = store.get(phone);
        if (entry == null) return false;

        if (Instant.now().isAfter(entry.expiresAt()) || entry.attempts() >= MAX_VERIFY_ATTEMPTS) {
            store.remove(phone);
            return false;
        }

        boolean matches = MessageDigest.isEqual(
                entry.code().getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8));

        if (matches) {
            store.remove(phone);
            return true;
        }

        store.put(phone, new OtpEntry(entry.code(), entry.expiresAt(), entry.sentAt(), entry.attempts() + 1));
        return false;
    }

    private void checkAndRecord(Map<String, Deque<Instant>> windows, String key, int max, String message) {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.HOURS);
        Deque<Instant> sends = windows.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        while (!sends.isEmpty() && sends.peekFirst().isBefore(cutoff)) sends.pollFirst();
        if (sends.size() >= max) throw new IllegalArgumentException(message);
        sends.addLast(Instant.now());
    }
}