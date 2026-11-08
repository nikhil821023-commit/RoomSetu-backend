package com.roomsetu.entity;

import com.roomsetu.entity.enums.LeaseType;
import com.roomsetu.entity.enums.ListingFeature;
import com.roomsetu.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false, length = 150)
    private String locality;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false)
    private Integer rent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaseType leaseType;

    @Column(nullable = false)
    private LocalDate availableFrom;

    @ElementCollection
    @CollectionTable(name = "listing_photos", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    @Column(nullable = false)
    @Builder.Default
    private Boolean filled = false;

    @Column(length = 1000)
    private String description;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    private Double latitude;
    private Double longitude;


    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @ElementCollection
    @CollectionTable(name = "listing_features", joinColumns = @JoinColumn(name = "listing_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "feature", length = 40)
    @Builder.Default
    private Set<ListingFeature> features = new HashSet<>();
}
