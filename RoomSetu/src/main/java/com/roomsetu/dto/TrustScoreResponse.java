package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrustScoreResponse {
    private Long userId;
    private int score;          // 0-100
    private boolean idVerified;
    private Double averageRating;
    private long reviewCount;
    private long openReportsAgainst;
}