package com.roomsetu.entity;

import com.roomsetu.entity.enums.Role;
import com.roomsetu.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( unique = true, length = 15)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean admin = false;

    private String aadhaarLast4;
    private Instant aadhaarVerifiedAt;
    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }


    @Column(length = 150)
    private String workEmail;


    private Instant workEmailVerifiedAt;
}