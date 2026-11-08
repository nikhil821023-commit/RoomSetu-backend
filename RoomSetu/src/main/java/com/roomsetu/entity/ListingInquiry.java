package com.roomsetu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A "lead" on a listing — distinct from Match, which is profile-to-profile
 * compatibility. A seeker can inquire about a listing without going through
 * the roommate-matching flow at all (e.g. a host's spare room where
 * compatibility scoring is secondary to just wanting the room).
 */
@Entity
@Table(name = "listing_inquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquirer_id", nullable = false)
    private User inquirer;

    @Column(length = 500)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}