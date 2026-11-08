package com.roomsetu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Kept deliberately simple for Phase 4: one payer, one amount, always split
 * 50/50 between the two people on the match. Custom split ratios and
 * multi-way splits (for >2 person households) are a real future need but
 * add real complexity — revisit once actual usage shows 50/50 isn't enough.
 */
@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false, length = 150)
    private String description;

    @Column(nullable = false)
    private Integer amount; // rupees, whole numbers — matches the rest of the schema (Listing.rent is Integer too)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy;

    @Column(nullable = false, length = 20)
    private String category; // "rent" | "groceries" | "utilities" | "other" — free text, not an enum, deliberately

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}