package com.roomsetu.repository;

import com.roomsetu.entity.PartnerOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerOrganizationRepository extends JpaRepository<PartnerOrganization, Long> {
    Optional<PartnerOrganization> findBySignupCode(String signupCode);
}