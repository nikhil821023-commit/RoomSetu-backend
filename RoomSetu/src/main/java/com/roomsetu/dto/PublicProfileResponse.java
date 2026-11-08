package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicProfileResponse {
    private Long userId;
    private String name;
    private String role;
    private String photoUrl;
    private String bio;
    private String locality;
    private Instant memberSince;
    private boolean self;

    // Lifestyle answers, raw enum names; the frontend maps them to labels
    private String sleepSchedule;
    private String workFromHome;
    private String foodPreference;
    private String cleanliness;
    private Boolean smokes;
    private Boolean drinks;
    private Boolean hasPets;

    // Badges: only things actually checked
    private boolean phoneVerified;
    private String workEmailDomain;

    private Double averageRating;
    private long reviewCount;
    private List<ReviewResponse> reviews;
    private List<ListingResponse> listings;

    private String matchStatus;       // null, PENDING, ACCEPTED or REJECTED (viewer's match with this person)
    private boolean viewerCanReview;
}