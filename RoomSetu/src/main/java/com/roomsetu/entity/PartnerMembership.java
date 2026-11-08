package com.roomsetu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "partner_memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_org_id", nullable = false)
    private PartnerOrganization partnerOrganization;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        this.joinedAt = Instant.now();
    }
}