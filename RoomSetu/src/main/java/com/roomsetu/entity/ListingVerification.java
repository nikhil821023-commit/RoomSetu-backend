package com.roomsetu.entity;

import com.roomsetu.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Mirrors the user-facing Verification entity from Phase 1, but for property ownership
 * proof — kept as a separate entity rather than reusing Verification, since a listing
 * isn't a User and forcing one table to serve both would mean nullable FKs on every row.
 */
@Entity
@Table(name = "listing_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(nullable = false, length = 40)
    private String docType; // e.g. OWNERSHIP_PROOF, RENTAL_AUTHORITY_LETTER

    @Column(nullable = false, length = 500)
    private String docUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus reviewStatus = VerificationStatus.PENDING;

    private String reviewedBy;

    private Instant reviewedAt;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    @PrePersist
    void onCreate() {
        this.submittedAt = Instant.now();
    }
}