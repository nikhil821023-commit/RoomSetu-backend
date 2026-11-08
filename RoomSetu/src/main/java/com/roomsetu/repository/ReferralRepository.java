package com.roomsetu.repository;

import com.roomsetu.entity.Referral;
import com.roomsetu.entity.enums.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralRepository extends JpaRepository<Referral, Long> {
    Optional<Referral> findByReferredUserId(Long referredUserId);
    List<Referral> findByReferralCodeOwnerId(Long ownerId);
    long countByReferralCodeOwnerIdAndStatus(Long ownerId, ReferralStatus status);
}