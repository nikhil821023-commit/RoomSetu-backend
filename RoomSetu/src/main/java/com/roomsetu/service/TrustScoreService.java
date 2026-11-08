package com.roomsetu.service;

import com.roomsetu.dto.TrustScoreResponse;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.ReportStatus;
import com.roomsetu.entity.enums.ReportTargetType;
import com.roomsetu.entity.enums.VerificationStatus;
import com.roomsetu.repository.FraudReportRepository;
import com.roomsetu.repository.ReviewRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Illustrative composite score, shown on the user's own profile (see prototype's
 * "Trust score 83/100" widget). Weighting is a starting point, not a finished formula —
 * revisit once there's enough real review/report volume to know what should matter more.
 */
@Service
public class TrustScoreService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final FraudReportRepository fraudReportRepository;

    public TrustScoreService(UserRepository userRepository, ReviewRepository reviewRepository,
                             FraudReportRepository fraudReportRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.fraudReportRepository = fraudReportRepository;
    }

    public TrustScoreResponse compute(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


        boolean idVerified = user.getWorkEmailVerifiedAt() != null
                || user.getVerificationStatus() == VerificationStatus.VERIFIED;
        Double avgRating = reviewRepository.averageRatingFor(userId);
        long reviewCount = reviewRepository.countByRevieweeId(userId);
        long openReports = fraudReportRepository.countByTargetTypeAndTargetIdAndStatus(
                ReportTargetType.USER, userId, ReportStatus.OPEN);

        int score = 0;
        score += idVerified ? 40 : 0;
        score += avgRating != null ? (int) Math.round((avgRating / 5.0) * 40) : 0;
        score += reviewCount > 0 ? Math.min(20, (int) reviewCount * 4) : 0; // volume bonus, capped
        score -= (int) Math.min(score, openReports * 10); // never goes below 0

        return new TrustScoreResponse(userId, score, idVerified, avgRating, reviewCount, openReports);
    }
}