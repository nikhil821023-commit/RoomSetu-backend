package com.roomsetu.service;

import com.roomsetu.dto.FraudReportRequest;
import com.roomsetu.entity.FraudReport;
import com.roomsetu.entity.Listing;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.ReportStatus;
import com.roomsetu.entity.enums.ReportTargetType;
import com.roomsetu.entity.enums.VerificationStatus;
import com.roomsetu.repository.FraudReportRepository;
import com.roomsetu.repository.ListingRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FraudReportService {

    // Auto-flag threshold: enough independent reports and the listing is pulled from
    // the live feed pending review, without waiting for an admin to notice it manually.
    private static final long AUTO_FLAG_THRESHOLD = 3;

    private final FraudReportRepository fraudReportRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public FraudReportService(FraudReportRepository fraudReportRepository,
                              UserRepository userRepository,
                              ListingRepository listingRepository) {
        this.fraudReportRepository = fraudReportRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    public FraudReport submit(Long reporterId, FraudReportRequest req) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

        if (req.getTargetType() == ReportTargetType.USER) {
            if (req.getTargetId().equals(reporterId)) {
                throw new IllegalArgumentException("You can't report yourself");
            }
            if (!userRepository.existsById(req.getTargetId())) {
                throw new IllegalArgumentException("That user no longer exists");
            }
        } else if (!listingRepository.existsById(req.getTargetId())) {
            throw new IllegalArgumentException("That listing no longer exists");
        }

        // One open report per person per target. This also makes the auto-flag
        // threshold count distinct reporters instead of repeat clicks from one person.
        if (fraudReportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndStatus(
                reporterId, req.getTargetType(), req.getTargetId(), ReportStatus.OPEN)) {
            throw new IllegalArgumentException("You've already reported this. Our team is reviewing it");
        }

        FraudReport report = FraudReport.builder()
                .reporter(reporter)
                .targetType(req.getTargetType())
                .targetId(req.getTargetId())
                .reason(req.getReason())
                .status(ReportStatus.OPEN)
                .build();

        FraudReport saved = fraudReportRepository.save(report);
        maybeAutoFlag(req.getTargetType(), req.getTargetId());
        return saved;
    }

    // Admin-only. For a listing report, "resolved" now actually takes the listing down.
    public FraudReport review(Long reportId, boolean resolved, String reviewerName) {
        FraudReport report = fraudReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setStatus(resolved ? ReportStatus.RESOLVED : ReportStatus.DISMISSED);
        report.setReviewedBy(reviewerName);
        report.setReviewedAt(Instant.now());
        FraudReport saved = fraudReportRepository.save(report);

        if (resolved && report.getTargetType() == ReportTargetType.LISTING) {
            listingRepository.findById(report.getTargetId()).ifPresent(l -> {
                l.setVerificationStatus(VerificationStatus.REJECTED);
                listingRepository.save(l);
            });
        }
        return saved;
    }

    private void maybeAutoFlag(ReportTargetType targetType, Long targetId) {
        long openCount = fraudReportRepository.countByTargetTypeAndTargetIdAndStatus(
                targetType, targetId, ReportStatus.OPEN);

        if (openCount < AUTO_FLAG_THRESHOLD) return;

        if (targetType == ReportTargetType.LISTING) {
            listingRepository.findById(targetId).ifPresent(listing -> {
                listing.setVerificationStatus(VerificationStatus.FLAGGED);
                listingRepository.save(listing);
            });
        } else {
            userRepository.findById(targetId).ifPresent(user -> {
                user.setVerificationStatus(VerificationStatus.FLAGGED);
                userRepository.save(user);
            });
        }
    }



    public List<FraudReport> openQueue() {
        return fraudReportRepository.findByStatus(ReportStatus.OPEN);
    }
}