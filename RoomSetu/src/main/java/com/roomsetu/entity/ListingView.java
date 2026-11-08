package com.roomsetu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One row per view. Deliberately not deduplicated by viewer in Phase 5 — a host
 * wanting to know "how many times has this been looked at" cares about raw
 * interest volume, not unique visitors. Revisit if analytics need unique-viewer
 * counts later; that's a GROUP BY away, not a schema change.
 */
@Entity
@Table(name = "listing_views")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    // Nullable: browsing can happen before a full profile exists, and we don't
    // want to lose the view event just because the viewer isn't identified yet.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id")
    private User viewer;

    @Column(nullable = false, updatable = false)
    private Instant viewedAt;

    @PrePersist
    void onCreate() {
        this.viewedAt = Instant.now();
    }
}