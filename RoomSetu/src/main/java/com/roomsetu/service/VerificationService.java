package com.roomsetu.service;

import com.roomsetu.entity.User;
import com.roomsetu.entity.Verification;
import com.roomsetu.entity.enums.VerificationStatus;
import com.roomsetu.repository.UserRepository;
import com.roomsetu.repository.VerificationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;

    public VerificationService(VerificationRepository verificationRepository, UserRepository userRepository) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
    }

    public Verification submit(Long userId, String docType, String docUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setVerificationStatus(VerificationStatus.PENDING);
        userRepository.save(user);

        Verification v = Verification.builder()
                .user(user)
                .docType(docType)
                .docUrl(docUrl)
                .reviewStatus(VerificationStatus.PENDING)
                .build();

        return verificationRepository.save(v);
    }

    // Admin-only action — wire this behind an admin role check before going live
    public Verification review(Long verificationId, boolean approve, String reviewerName) {
        Verification v = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification record not found"));

        VerificationStatus outcome = approve ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED;
        v.setReviewStatus(outcome);
        v.setReviewedBy(reviewerName);
        v.setReviewedAt(Instant.now());
        verificationRepository.save(v);

        User user = v.getUser();
        user.setVerificationStatus(outcome);
        userRepository.save(user);

        return v;
    }

    public List<Verification> pendingQueue() {
        return verificationRepository.findByReviewStatus(VerificationStatus.PENDING);
    }
}