package com.roomsetu.repository;

import com.roomsetu.entity.PartnerMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerMembershipRepository extends JpaRepository<PartnerMembership, Long> {
    long countByPartnerOrganizationId(Long partnerOrgId);
}