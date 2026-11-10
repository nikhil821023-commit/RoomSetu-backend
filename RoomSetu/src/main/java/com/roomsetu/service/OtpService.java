package com.roomsetu.service;

import com.roomsetu.mail.EmailSender;
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
import java.util.function.Predicate;

/**
 * One code store for phone and email. Email keys are prefixed "email:" so they can never
 * collide with phone numbers. Codes and limits live in memory: lost on restart and not shared
 * across servers. Fine for one instance; move to Redis before running more than one.
 */
@Service
public class OtpService {

    private static final int RESEND_COOLDOWN_SECONDS = 30;
    private static final int MAX_SENDS_PER_TARGET_PER_HOUR = 5;
    private static final int MAX_SENDS_PER_IP_PER_HOUR = 15;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    @Value("${roomsetu.otp.expiry-seconds:300}")
    private long expirySeconds;

    @Value("${roomsetu.otp.console-fallback:true}")
    private boolean consoleFallback;

    @Value("${roomsetu.otp.dev-numbers:}")
    private String devNumbersCsv;

    private record OtpEntry(String code, Instant expiresAt, Instant sentAt, int attempts) {}

    private final SmsGateway smsGateway;
    private final EmailSender emailSender;
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> targetSends = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> ipSends = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public OtpService(SmsGateway smsGateway, EmailSender emailSender) {
        this.smsGateway = smsGateway;
        this.emailSender = emailSender;
    }

    // ---------- phone ----------

    public void generateAndSend(String phone, String clientIp) {
        issue(phone, clientIp,
                code -> {
                    if (isDevNumber(phone)) {
                        // Free path for test accounts: no SMS, the code prints in the server logs.
                        System.out.printf("[OTP] (dev number) %s for %s%n", code, phone);
                        return true;
                    }
                    return smsGateway.sendOtp(phone, code);
                },
                phone,
                "Too many codes requested for this number. Try again in an hour",
                "We couldn't send the SMS right now. Please try again in a moment");
    }

    public boolean verify(String phone, String code) {
        return check(phone, code);
    }

    // ---------- email ----------

    /** canonicalEmail identifies the inbox. deliverTo is the address exactly as typed. */
    public void generateAndSendEmail(String canonicalEmail, String deliverTo, String clientIp) {
        issue("email:" + canonicalEmail, clientIp,
                code -> emailSender.send(deliverTo, "Your RoomSetu login code",
                        "Your RoomSetu code is " + code + ". It expires in "
                                + Math.max(1, expirySeconds / 60) + " minutes.\n\n"
                                + "If you didn't request this, you can ignore this email."),
                deliverTo,
                "Too many codes requested for this email. Try again in an hour",
                "We couldn't send the email right now. Please try again in a moment");
    }

    public boolean verifyEmail(String canonicalEmail, String code) {
        return check("email:" + canonicalEmail, code);
    }

    // ---------- shared ----------

    private void issue(String storeKey, String clientIp, Predicate<String> deliver,
                       String consoleLabel, String limitMessage, String failureMessage) {
        Instant now = Instant.now();

        OtpEntry existing = store.get(storeKey);
        if (existing != null && now.isBefore(existing.sentAt().plusSeconds(RESEND_COOLDOWN_SECONDS))) {
            throw new IllegalArgumentException("Please wait 30 seconds before requesting another code");
        }
        checkAndRecord(targetSends, storeKey, MAX_SENDS_PER_TARGET_PER_HOUR, limitMessage);
        checkAndRecord(ipSends, clientIp, MAX_SENDS_PER_IP_PER_HOUR,
                "Too many requests from your network. Try again later");

        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(storeKey, new OtpEntry(code, now.plusSeconds(expirySeconds), now, 0));

        boolean delivered;
        try {
            delivered = deliver.test(code);
        } catch (Exception e) {
            delivered = false;
        }

        if (!delivered) {
            if (consoleFallback) {
                // Dev only. Set OTP_CONSOLE_FALLBACK=false so a failed send shows as an error instead.
                System.out.printf("[OTP] (console fallback) %s for %s%n", code, consoleLabel);
            } else {
                store.remove(storeKey);
                throw new IllegalArgumentException(failureMessage);
            }
        }
    }

    private boolean check(String storeKey, String code) {
        OtpEntry entry = store.get(storeKey);
        if (entry == null) return false;

        if (Instant.now().isAfter(entry.expiresAt()) || entry.attempts() >= MAX_VERIFY_ATTEMPTS) {
            store.remove(storeKey);
            return false;
        }

        boolean matches = MessageDigest.isEqual(
                entry.code().getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8));

        if (matches) {
            store.remove(storeKey);
            return true;
        }

        store.put(storeKey, new OtpEntry(entry.code(), entry.expiresAt(), entry.sentAt(), entry.attempts() + 1));
        return false;
    }

    private void checkAndRecord(Map<String, Deque<Instant>> windows, String key, int max, String message) {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.HOURS);
        Deque<Instant> sends = windows.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        while (!sends.isEmpty() && sends.peekFirst().isBefore(cutoff)) sends.pollFirst();
        if (sends.size() >= max) throw new IllegalArgumentException(message);
        sends.addLast(Instant.now());
    }

    private boolean isDevNumber(String phone) {
        if (devNumbersCsv == null || devNumbersCsv.isBlank()) return false;
        return Arrays.stream(devNumbersCsv.split(",")).map(String::trim).anyMatch(phone::equals);
    }
}