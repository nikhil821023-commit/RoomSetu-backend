package com.roomsetu.repository;

import com.roomsetu.entity.Verification;
import com.roomsetu.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    List<Verification> findByUserId(Long userId);
    List<Verification> findByReviewStatus(VerificationStatus status);
}