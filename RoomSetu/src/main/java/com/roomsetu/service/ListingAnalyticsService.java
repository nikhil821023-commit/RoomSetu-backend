package com.roomsetu.service;

import com.roomsetu.dto.ListingAnalyticsResponse;
import com.roomsetu.dto.ListingInquiryRequest;
import com.roomsetu.entity.Listing;
import com.roomsetu.entity.ListingInquiry;
import com.roomsetu.entity.ListingView;
import com.roomsetu.entity.User;
import com.roomsetu.repository.ListingInquiryRepository;
import com.roomsetu.repository.ListingRepository;
import com.roomsetu.repository.ListingViewRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ListingAnalyticsService {

    private final ListingViewRepository listingViewRepository;
    private final ListingInquiryRepository listingInquiryRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final BlockService blockService;

    public ListingAnalyticsService(ListingViewRepository listingViewRepository,
                                   ListingInquiryRepository listingInquiryRepository,
                                   ListingRepository listingRepository,
                                   UserRepository userRepository, BlockService blockService) {
        this.listingViewRepository = listingViewRepository;
        this.listingInquiryRepository = listingInquiryRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;

        this.blockService = blockService;
    }

    /** viewerId is nullable on purpose — see ListingView entity note. */
    public void recordView(Long listingId, Long viewerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        User viewer = viewerId != null ? userRepository.getReferenceById(viewerId) : null;

        listingViewRepository.save(ListingView.builder()
                .listing(listing)
                .viewer(viewer)
                .build());
    }

    public ListingInquiry recordInquiry(Long listingId, Long inquirerId, ListingInquiryRequest req) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        User inquirer = userRepository.findById(inquirerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (listing.getHost().getId().equals(inquirerId)) {
            throw new IllegalArgumentException("You can't inquire about your own listing");
        }

        if (Boolean.TRUE.equals(listing.getDeleted()) || Boolean.TRUE.equals(listing.getFilled())) {
            throw new IllegalArgumentException("This place is no longer available");
        }
        blockService.requireNotBlocked(inquirerId, listing.getHost().getId());

        return listingInquiryRepository.save(ListingInquiry.builder()
                .listing(listing)
                .inquirer(inquirer)
                .message(req.getMessage())
                .build());
    }

    public ListingAnalyticsResponse analyticsFor(Listing listing) {
        long views = listingViewRepository.countByListingId(listing.getId());
        long inquiries = listingInquiryRepository.countByListingId(listing.getId());

        // Guard against divide-by-zero on a fresh listing with no views yet —
        // 0% conversion is the correct answer, not an error or NaN.
        double conversionPct = views == 0 ? 0.0 : (inquiries / (double) views) * 100;

        return new ListingAnalyticsResponse(
                listing.getId(), views, inquiries,
                Math.round(conversionPct * 10) / 10.0, // one decimal place
                listing.getFilled()
        );
    }
}