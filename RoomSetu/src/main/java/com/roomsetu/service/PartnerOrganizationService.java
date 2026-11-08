package com.roomsetu.service;

import com.roomsetu.dto.PartnerOrganizationRequest;
import com.roomsetu.entity.PartnerMembership;
import com.roomsetu.entity.PartnerOrganization;
import com.roomsetu.entity.User;
import com.roomsetu.repository.PartnerMembershipRepository;
import com.roomsetu.repository.PartnerOrganizationRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PartnerOrganizationService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    private final PartnerOrganizationRepository partnerOrganizationRepository;
    private final PartnerMembershipRepository partnerMembershipRepository;
    private final UserRepository userRepository;

    public PartnerOrganizationService(PartnerOrganizationRepository partnerOrganizationRepository,
                                      PartnerMembershipRepository partnerMembershipRepository,
                                      UserRepository userRepository) {
        this.partnerOrganizationRepository = partnerOrganizationRepository;
        this.partnerMembershipRepository = partnerMembershipRepository;
        this.userRepository = userRepository;
    }

    // Admin-only — same guard requirement as every other admin.* method across Phases 1-3
    public PartnerOrganization create(PartnerOrganizationRequest req) {
        String code;
        do {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            code = sb.toString();
        } while (partnerOrganizationRepository.findBySignupCode(code).isPresent());

        return partnerOrganizationRepository.save(PartnerOrganization.builder()
                .name(req.getName())
                .type(req.getType())
                .locality(req.getLocality())
                .signupCode(code)
                .build());
    }

    /**
     * Called at signup if the new user entered a partner signup code (college/corporate
     * onboarding channel). Wire this into AuthService.registerNewUser alongside the
     * referral-code hook — see the note at the bottom of this response.
     */
    public void applyPartnerCode(Long newUserId, String signupCode) {
        PartnerOrganization org = partnerOrganizationRepository.findBySignupCode(signupCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid partner signup code"));

        User user = userRepository.getReferenceById(newUserId);

        partnerMembershipRepository.save(PartnerMembership.builder()
                .partnerOrganization(org)
                .user(user)
                .build());
    }

    public long memberCount(Long partnerOrgId) {
        return partnerMembershipRepository.countByPartnerOrganizationId(partnerOrgId);
    }
}