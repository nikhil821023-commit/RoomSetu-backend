package com.roomsetu.entity;

import com.roomsetu.entity.enums.ReportStatus;
import com.roomsetu.entity.enums.ReportTargetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "fraud_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;

    // Points at either a User.id or a Listing.id depending on targetType —
    // kept as a plain Long rather than two nullable FK columns to keep this table simple.
    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.OPEN;

    private String reviewedBy;

    private Instant reviewedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}