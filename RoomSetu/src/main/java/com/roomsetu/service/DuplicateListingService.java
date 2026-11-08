package com.roomsetu.service;

import com.roomsetu.entity.Listing;
import com.roomsetu.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Flags likely-fraudulent listings before they go live — reused photos or a reused
 * address under a different host is the single most common signal of a fake listing
 * (Product doc, Section 7.2). This runs synchronously on listing creation; a real-scale
 * version would move this to an async job so listing creation doesn't wait on the scan.
 */
@Service
public class DuplicateListingService {

    private final ListingRepository listingRepository;

    public DuplicateListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public record DuplicateCheckResult(boolean suspicious, String reason) {}

    public DuplicateCheckResult check(Listing candidate) {
        List<Listing> sameAddress = listingRepository.findByAddressIgnoreCase(candidate.getAddress());
        boolean addressReusedByOther = sameAddress.stream()
                .anyMatch(l -> !l.getHost().getId().equals(candidate.getHost().getId()));

        if (addressReusedByOther) {
            return new DuplicateCheckResult(true,
                    "This address is already listed under a different host account");
        }

        if (candidate.getPhotoUrls() != null && !candidate.getPhotoUrls().isEmpty()) {
            List<Listing> photoMatches = listingRepository.findByPhotoUrlsInAndHostIdNot(
                    candidate.getPhotoUrls(), candidate.getHost().getId());
            if (!photoMatches.isEmpty()) {
                return new DuplicateCheckResult(true,
                        "One or more photos in this listing are already used by another host's listing");
            }
        }

        return new DuplicateCheckResult(false, null);
    }
}