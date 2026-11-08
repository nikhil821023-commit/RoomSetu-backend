package com.roomsetu.entity;

import com.roomsetu.entity.enums.ReferralStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Reward triggers on the referred user's FIRST accepted match, not on signup —
 * this is a deliberate anti-spam choice from the product doc (Section 16):
 * rewarding signups alone invites fake-account referral farming.
 */
@Entity
@Table(name = "referrals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_code_id", nullable = false)
    private ReferralCode referralCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_user_id", nullable = false, unique = true)
    private User referredUser; // unique — a user can only ever be referred once

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReferralStatus status = ReferralStatus.SIGNED_UP;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant rewardedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}