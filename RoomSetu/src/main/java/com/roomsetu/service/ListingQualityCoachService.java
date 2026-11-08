package com.roomsetu.service;

import com.roomsetu.dto.ListingQualityReportResponse;
import com.roomsetu.entity.Listing;
import com.roomsetu.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Deliberately rule-based, not AI — a host's listing quality is a data question
 * (photo count, price vs. comparable listings, description length), not a language
 * generation problem. No AI call needed or wanted here; it would only add latency
 * and cost for something a handful of if-statements already does reliably.
 */
@Service
public class ListingQualityCoachService {

    private static final int MIN_RECOMMENDED_PHOTOS = 4;
    private static final int MIN_DESCRIPTION_LENGTH = 60;
    private static final double PRICE_HIGH_THRESHOLD = 1.15; // 15% above locality average

    private final ListingRepository listingRepository;

    public ListingQualityCoachService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public ListingQualityReportResponse assess(Listing listing) {
        List<String> suggestions = new ArrayList<>();
        int score = 100;

        int photoCount = listing.getPhotoUrls() == null ? 0 : listing.getPhotoUrls().size();
        if (photoCount < MIN_RECOMMENDED_PHOTOS) {
            int missing = MIN_RECOMMENDED_PHOTOS - photoCount;
            suggestions.add("Add " + missing + " more photo" + (missing == 1 ? "" : "s")
                    + " — listings with at least " + MIN_RECOMMENDED_PHOTOS + " photos get noticeably more inquiries.");
            score -= 15 * missing;
        }

        int descLength = listing.getDescription() == null ? 0 : listing.getDescription().length();
        if (descLength < MIN_DESCRIPTION_LENGTH) {
            suggestions.add("Your description is quite short — mention nearby landmarks, "
                    + "what's included in rent, and what kind of flatmate you're looking for.");
            score -= 10;
        }

        OptionalDouble avgRent = listingRepository
                .findByLocalityIgnoreCaseAndVerificationStatus(listing.getLocality(),
                        com.roomsetu.entity.enums.VerificationStatus.VERIFIED)
                .stream()
                .filter(l -> !l.getId().equals(listing.getId()))
                .filter(l -> l.getLeaseType() == listing.getLeaseType())
                .mapToInt(Listing::getRent)
                .average();

        if (avgRent.isPresent() && listing.getRent() > avgRent.getAsDouble() * PRICE_HIGH_THRESHOLD) {
            int pctAbove = (int) Math.round(((listing.getRent() / avgRent.getAsDouble()) - 1) * 100);
            suggestions.add("Your price is about " + pctAbove + "% above similar " + listing.getLeaseType()
                    + " listings nearby — consider whether that's intentional.");
            score -= 10;
        }

        if (suggestions.isEmpty()) {
            suggestions.add("This listing looks complete — nothing obvious to improve right now.");
        }

        return new ListingQualityReportResponse(Math.max(0, score), suggestions);
    }
}