package com.roomsetu.entity;

import com.roomsetu.entity.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "matches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id", "target_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who initiated the match request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // The user being requested
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @Column(nullable = false)
    private Integer compatibilityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchStatus status = MatchStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant respondedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}