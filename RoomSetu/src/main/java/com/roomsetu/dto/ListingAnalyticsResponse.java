package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListingAnalyticsResponse {
    private Long listingId;
    private long viewCount;
    private long inquiryCount;
    private double viewToInquiryConversionPct; // 0 if no views yet — see service for the guard
    private boolean filled;
}