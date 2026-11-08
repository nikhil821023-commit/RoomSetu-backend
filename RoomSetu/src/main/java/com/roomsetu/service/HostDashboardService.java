package com.roomsetu.service;

import com.roomsetu.dto.HostDashboardSummaryResponse;
import com.roomsetu.dto.ListingAnalyticsResponse;
import com.roomsetu.dto.ListingInquiryResponse;
import com.roomsetu.entity.Listing;
import com.roomsetu.repository.ListingInquiryRepository;
import com.roomsetu.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostDashboardService {

    private final ListingRepository listingRepository;
    private final ListingInquiryRepository listingInquiryRepository;
    private final ListingAnalyticsService listingAnalyticsService;

    public HostDashboardService(ListingRepository listingRepository,
                                ListingInquiryRepository listingInquiryRepository,
                                ListingAnalyticsService listingAnalyticsService) {
        this.listingRepository = listingRepository;
        this.listingInquiryRepository = listingInquiryRepository;
        this.listingAnalyticsService = listingAnalyticsService;
    }

    public HostDashboardSummaryResponse summaryFor(Long hostId) {
        List<Listing> listings = listingRepository.findByHostId(hostId);

        List<ListingAnalyticsResponse> perListing = listings.stream()
                .map(listingAnalyticsService::analyticsFor)
                .toList();

        int filled = (int) listings.stream().filter(Listing::getFilled).count();

        long totalViews = perListing.stream().mapToLong(ListingAnalyticsResponse::getViewCount).sum();
        long totalInquiries = perListing.stream().mapToLong(ListingAnalyticsResponse::getInquiryCount).sum();

        List<ListingInquiryResponse> recentInquiries = listingInquiryRepository
                .findByListingHostIdOrderByCreatedAtDesc(hostId)
                .stream()
                .limit(10)
                .map(ListingInquiryResponse::from)
                .toList();

        return new HostDashboardSummaryResponse(
                listings.size(),
                filled,
                listings.size() - filled,
                totalViews,
                totalInquiries,
                perListing,
                recentInquiries
        );
    }

    public Listing setFilled(Long listingId, Long hostId, boolean filled) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (!listing.getHost().getId().equals(hostId)) {
            throw new IllegalArgumentException("Only the listing's host can update occupancy");
        }

        listing.setFilled(filled);
        return listingRepository.save(listing);
    }
}