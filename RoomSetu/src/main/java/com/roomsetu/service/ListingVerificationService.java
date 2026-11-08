package com.roomsetu.service;

import com.roomsetu.entity.Listing;
import com.roomsetu.entity.ListingVerification;
import com.roomsetu.entity.enums.VerificationStatus;
import com.roomsetu.repository.ListingRepository;
import com.roomsetu.repository.ListingVerificationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ListingVerificationService {

    private final ListingVerificationRepository verificationRepository;
    private final ListingRepository listingRepository;

    public ListingVerificationService(ListingVerificationRepository verificationRepository,
                                      ListingRepository listingRepository) {
        this.verificationRepository = verificationRepository;
        this.listingRepository = listingRepository;
    }

    public ListingVerification submit(Long listingId, Long hostId, String docType, String docUrl) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (!listing.getHost().getId().equals(hostId)) {
            throw new IllegalArgumentException("Only the listing's host can submit ownership proof");
        }

        ListingVerification v = ListingVerification.builder()
                .listing(listing)
                .docType(docType)
                .docUrl(docUrl)
                .reviewStatus(VerificationStatus.PENDING)
                .build();

        return verificationRepository.save(v);
    }

    // Admin-only — guard with a role check before deploying, same as VerificationService in Phase 1
    public ListingVerification review(Long verificationId, boolean approve, String reviewerName) {
        ListingVerification v = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification record not found"));

        VerificationStatus outcome = approve ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED;
        v.setReviewStatus(outcome);
        v.setReviewedBy(reviewerName);
        v.setReviewedAt(Instant.now());
        verificationRepository.save(v);

        Listing listing = v.getListing();
        listing.setVerificationStatus(outcome);
        listingRepository.save(listing);

        return v;
    }

    public List<ListingVerification> pendingQueue() {
        return verificationRepository.findByReviewStatus(VerificationStatus.PENDING);
    }
}