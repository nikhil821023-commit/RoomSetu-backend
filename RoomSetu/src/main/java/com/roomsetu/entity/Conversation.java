package com.roomsetu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One conversation per unique pair of users — same pattern as Match (Phase 2)
 * and Block (Phase 3): a unique constraint on the pair, checked both directions
 * in queries since (A,B) and (B,A) are the same conversation.
 */
@Entity
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_a_id", "user_b_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    @Column(name = "user_a_cleared_at")
    private Instant userAClearedAt;

    @Column(name = "user_b_cleared_at")
    private Instant userBClearedAt;

    @Column(name = "user_a_last_read_at")
    private Instant userALastReadAt;

    @Column(name = "user_b_last_read_at")
    private Instant userBLastReadAt;
}