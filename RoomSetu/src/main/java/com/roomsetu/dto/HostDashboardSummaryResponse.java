package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HostDashboardSummaryResponse {
    private int totalListings;
    private int filledListings;
    private int vacantListings;
    private long totalViewsAcrossListings;
    private long totalInquiriesAcrossListings;
    private List<ListingAnalyticsResponse> perListing;
    private List<ListingInquiryResponse> recentInquiries; // most recent 10, across all listings — see service
}