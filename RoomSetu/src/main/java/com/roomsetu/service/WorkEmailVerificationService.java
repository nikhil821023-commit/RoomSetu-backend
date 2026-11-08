package com.roomsetu.service;

import com.roomsetu.entity.User;
import com.roomsetu.mail.EmailSender;
import com.roomsetu.repository.UserRepository;
import com.roomsetu.util.EmailDomains;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Pending codes live in memory, same trade-off as the phone OTP store: lost on
 * restart and not shared across servers. Fine for one instance; move to Redis
 * before running more than one.
 */
@Service
public class WorkEmailVerificationService {

    private static final int CODE_TTL_SECONDS = 600;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private record Pending(String email, String code, Instant expiresAt, Instant sentAt, int attempts) {}

    private final Map<Long, Pending> pending = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailSender emailSender;

    @Value("${roomsetu.otp.console-fallback:true}")
    private boolean consoleFallback;

    public WorkEmailVerificationService(UserRepository userRepository, EmailSender emailSender) {
        this.userRepository = userRepository;
        this.emailSender = emailSender;
    }

    public void requestCode(Long userId, String rawEmail) {
        String email = rawEmail.trim().toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Enter a valid email address");
        }

        String domain = EmailDomains.domainOf(email);
        if (EmailDomains.isFreeProvider(domain)) {
            throw new IllegalArgumentException(
                    "Use your work or college email. Personal addresses like Gmail or Yahoo can't be used for this badge");
        }
        if (userRepository.isWorkEmailTakenByAnother(email, userId)) {
            throw new IllegalArgumentException("That email is already verified on another account");
        }

        Pending existing = pending.get(userId);
        if (existing != null && Instant.now().isBefore(existing.sentAt().plusSeconds(RESEND_COOLDOWN_SECONDS))) {
            throw new IllegalArgumentException("Please wait a minute before requesting another code");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant now = Instant.now();
        pending.put(userId, new Pending(email, code, now.plusSeconds(CODE_TTL_SECONDS), now, 0));

        boolean sent = emailSender.send(email, "Your RoomSetu verification code",
                "Your RoomSetu code is " + code + ". It expires in 10 minutes.\n\n"
                        + "If you didn't request this, you can ignore this email.");

        if (!sent) {
            if (consoleFallback) {
                System.out.printf("[EMAIL] (console fallback) code %s for %s%n", code, email);
            } else {
                pending.remove(userId);
                throw new IllegalArgumentException("We couldn't send the email right now. Please try again in a moment");
            }
        }
    }

    /** Returns the verified domain on success. */
    public String confirmCode(Long userId, String submittedCode) {
        Pending p = pending.get(userId);
        if (p == null) throw new IllegalArgumentException("Request a code first");

        if (Instant.now().isAfter(p.expiresAt())) {
            pending.remove(userId);
            throw new IllegalArgumentException("That code has expired. Request a new one");
        }
        if (p.attempts() >= MAX_ATTEMPTS) {
            pending.remove(userId);
            throw new IllegalArgumentException("Too many wrong attempts. Request a new code");
        }

        boolean match = MessageDigest.isEqual(
                p.code().getBytes(StandardCharsets.UTF_8),
                submittedCode.trim().getBytes(StandardCharsets.UTF_8));

        if (!match) {
            pending.put(userId, new Pending(p.email(), p.code(), p.expiresAt(), p.sentAt(), p.attempts() + 1));
            throw new IllegalArgumentException("Incorrect code");
        }

        // Re-check at confirm time: someone else may have verified this email since the code was sent.
        if (userRepository.isWorkEmailTakenByAnother(p.email(), userId)) {
            pending.remove(userId);
            throw new IllegalArgumentException("That email is already verified on another account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setWorkEmail(p.email());
        user.setWorkEmailVerifiedAt(Instant.now());
        userRepository.save(user);
        pending.remove(userId);

        String domain = EmailDomains.domainOf(p.email());
        String verifiedEmail = p.email();
        String displayName = user.getName();

// Confirmation email. Sent in the background, and a mail failure never undoes the verification.
        CompletableFuture.runAsync(() -> emailSender.send(
                verifiedEmail,
                "Your email is verified on RoomSetu",
                "Hi " + displayName + ",\n\n"
                        + "Your email " + verifiedEmail + " is now verified on RoomSetu. "
                        + "A \"Verified @" + domain + "\" badge now shows on your profile.\n\n"
                        + "If you didn't do this, reply to this email and let us know."));

        return domain;
    }
}