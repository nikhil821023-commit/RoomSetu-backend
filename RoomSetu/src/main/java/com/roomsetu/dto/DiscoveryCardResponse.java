package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryCardResponse {
    private Long userId;
    private String name;
    private String locality;
    private String verificationStatus;
    private String workEmailDomain;
    private String photoUrl;
    private Integer compatibilityScore;
    private Map<String, Integer> scoreBreakdown;
    private String bio;
    private String distanceLabel;   // band like "2-5 km", or null if either side has no saved location
}