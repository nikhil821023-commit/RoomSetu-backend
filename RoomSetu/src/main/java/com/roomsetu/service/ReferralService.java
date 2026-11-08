package com.roomsetu.service;

import com.roomsetu.dto.ReferralStatsResponse;
import com.roomsetu.entity.Referral;
import com.roomsetu.entity.ReferralCode;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.ReferralStatus;
import com.roomsetu.repository.ReferralCodeRepository;
import com.roomsetu.repository.ReferralRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class ReferralService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no O/0/I/1 — avoids typos
    private final SecureRandom random = new SecureRandom();

    private final ReferralCodeRepository referralCodeRepository;
    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;

    public ReferralService(ReferralCodeRepository referralCodeRepository,
                           ReferralRepository referralRepository,
                           UserRepository userRepository) {
        this.referralCodeRepository = referralCodeRepository;
        this.referralRepository = referralRepository;
        this.userRepository = userRepository;
    }

    /** Idempotent — calling this again for a user who already has a code just returns it. */
    public String getOrCreateCode(Long userId) {
        return referralCodeRepository.findByOwnerId(userId)
                .map(ReferralCode::getCode)
                .orElseGet(() -> {
                    User owner = userRepository.getReferenceById(userId);
                    String code = generateUniqueCode();
                    referralCodeRepository.save(ReferralCode.builder().code(code).owner(owner).build());
                    return code;
                });
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            code = sb.toString();
        } while (referralCodeRepository.findByCode(code).isPresent());
        return code;
    }

    /**
     * Called at signup time if the new user entered a referral code.
     * Wire this into AuthService.registerNewUser (Phase 1) — see the note
     * at the bottom of this response for the exact hook point.
     */
    public void applyReferralCode(Long newUserId, String code) {
        ReferralCode referralCode = referralCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid referral code"));

        if (referralCode.getOwner().getId().equals(newUserId)) {
            throw new IllegalArgumentException("You can't refer yourself");
        }

        User referredUser = userRepository.getReferenceById(newUserId);

        Referral referral = Referral.builder()
                .referralCode(referralCode)
                .referredUser(referredUser)
                .status(ReferralStatus.SIGNED_UP)
                .build();

        referralRepository.save(referral);
    }

    /**
     * Called when a match reaches ACCEPTED (see MatchService.respond() in Phase 2 —
     * add a call to this at the point status flips to ACCEPTED). Rewards the referrer
     * of EITHER party in the match, the first time that referred user completes a match.
     * Deliberately a no-op if the user wasn't referred at all — most users won't be.
     */
    public void rewardIfEligible(Long userId) {
        referralRepository.findByReferredUserId(userId).ifPresent(referral -> {
            if (referral.getStatus() == ReferralStatus.REWARDED) return; // already rewarded, don't double-grant

            referral.setStatus(ReferralStatus.REWARDED);
            referral.setRewardedAt(Instant.now());
            referralRepository.save(referral);

            // TODO: this is where the actual reward is granted — a discount on
            // priority verification, a small cash credit, etc. Left as a hook
            // since the reward mechanism depends on which Phase 7 monetization
            // features actually ship first.
            System.out.printf("[REFERRAL] Reward triggered for referrer of user %d%n", userId);
        });
    }

    public ReferralStatsResponse statsFor(Long ownerId) {
        long total = referralRepository.findByReferralCodeOwnerId(ownerId).size();
        long rewarded = referralRepository.countByReferralCodeOwnerIdAndStatus(ownerId, ReferralStatus.REWARDED);
        long pending = total - rewarded;
        return new ReferralStatsResponse(total, rewarded, pending);
    }
}