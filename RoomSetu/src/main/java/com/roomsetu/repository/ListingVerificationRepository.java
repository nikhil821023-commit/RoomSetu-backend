package com.roomsetu.repository;

import com.roomsetu.entity.ListingVerification;
import com.roomsetu.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingVerificationRepository extends JpaRepository<ListingVerification, Long> {
    List<ListingVerification> findByListingId(Long listingId);
    List<ListingVerification> findByReviewStatus(VerificationStatus status);
}