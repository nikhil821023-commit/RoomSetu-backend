package com.roomsetu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roomsetu.entity.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer budgetMin;

    @Column(nullable = false)
    private Integer budgetMax;

    @Column(nullable = false, length = 100)
    private String locality;

    // ---- Compatibility signals (Section 8 of the product doc) ----
    @Enumerated(EnumType.STRING)
    private SleepSchedule sleepSchedule;

    @Enumerated(EnumType.STRING)
    private WorkFromHome workFromHome;

    @Enumerated(EnumType.STRING)
    private FoodPreference foodPreference;

    @Enumerated(EnumType.STRING)
    private Cleanliness cleanliness;

    private Boolean smokes;

    private Boolean drinks;

    private Boolean hasPets;

    private Boolean okWithPets;

    @Column(length = 500)
    private String bio;

    // Add alongside the existing @Column fields:
    @Column(length = 500)
    private String photoUrl;

    private Double latitude;
    private Double longitude;

    @Column(name = "location_label", length = 200)
    private String locationLabel;
}