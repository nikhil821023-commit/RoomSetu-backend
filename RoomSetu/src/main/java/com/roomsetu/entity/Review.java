package com.roomsetu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "reviews", uniqueConstraints = {
        // one review per reviewer-reviewee-match combination — stops rating spam
        @UniqueConstraint(columnNames = {"reviewer_id", "reviewee_id", "match_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    // Nullable: a review can exist without a matched tenancy (e.g. reviewing a host directly),
    // but linking it where possible is what makes the review trustworthy rather than anonymous noise.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(length = 500)
    private String comment;

    @Column(length = 100)
    private String tenancyPeriod;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}