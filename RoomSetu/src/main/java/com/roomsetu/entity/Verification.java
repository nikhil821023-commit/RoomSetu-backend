package com.roomsetu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roomsetu.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 40)
    private String docType; // e.g. AADHAAR, COLLEGE_ID, EMPLOYEE_ID

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