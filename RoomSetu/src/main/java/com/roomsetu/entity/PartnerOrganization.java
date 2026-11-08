package com.roomsetu.entity;

import com.roomsetu.entity.enums.PartnerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A college or company RoomSetu has an onboarding partnership with
 * (product doc Section 11.2 / Section 16 — campus ambassador & corporate
 * onboarding channels). The signupCode is what new-arrival batches use at
 * registration so you can measure which channel is actually bringing users in.
 */
@Entity
@Table(name = "partner_organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerOrganization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerType type;

    @Column(nullable = false, unique = true, length = 20)
    private String signupCode;

    @Column(nullable = false, length = 100)
    private String locality; // ties the partnership to a pilot micro-market

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}