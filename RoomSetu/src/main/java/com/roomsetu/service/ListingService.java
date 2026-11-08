package com.roomsetu.service;

import com.roomsetu.dto.ListingInquiryResponse;
import com.roomsetu.dto.ListingRequest;
import com.roomsetu.dto.ListingResponse;
import com.roomsetu.entity.Listing;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.VerificationStatus;
import com.roomsetu.repository.ListingInquiryRepository;
import com.roomsetu.repository.ListingRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final DuplicateListingService duplicateListingService;
    private final ListingInquiryRepository listingInquiryRepository;

    public ListingService(ListingRepository listingRepository,
                          UserRepository userRepository,
                          DuplicateListingService duplicateListingService, ListingInquiryRepository listingInquiryRepository ) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.duplicateListingService = duplicateListingService;
        this.listingInquiryRepository = listingInquiryRepository;
    }

    public Listing create(Long hostId, ListingRequest req) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new IllegalArgumentException("Host not found"));

        Listing listing = Listing.builder()
                .host(host)
                .locality(req.getLocality())
                .address(req.getAddress())
                .rent(req.getRent())
                .leaseType(req.getLeaseType())
                .availableFrom(req.getAvailableFrom())
                .photoUrls(req.getPhotoUrls())
                .verificationStatus(VerificationStatus.VERIFIED)
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .description(req.getDescription())
                .features(req.getFeatures() == null ? new HashSet<>() : new HashSet<>(req.getFeatures()))
                .build();

        // Phase 3: run the duplicate/fraud check before the listing is even saved as PENDING.
        var dupeCheck = duplicateListingService.check(listing);
        if (dupeCheck.suspicious()) {
            throw new IllegalArgumentException(dupeCheck.reason());
        }

        Listing saved = listingRepository.save(listing);

        if (dupeCheck.suspicious()) {
            // Surface the reason to whoever reviews the admin queue later.
            // (Kept as a log line for now — add a `flagReason` column on Listing
            // if you want this queryable rather than just logged.)
            System.out.printf("[FLAGGED] Listing %d: %s%n", saved.getId(), dupeCheck.reason());
        }

        return saved;
    }

    public List<Listing> browseVerifiedByLocality(String locality) {
        return listingRepository.findByLocalityContainingIgnoreCaseAndVerificationStatus(
                locality, VerificationStatus.VERIFIED);
    }

    public List<Listing> myListings(Long hostId) {
        return listingRepository.findByHostId(hostId).stream()
                .filter(l -> !Boolean.TRUE.equals(l.getDeleted()))
                .toList();
    }

    public List<Listing> flaggedQueue() {
        return listingRepository.findByVerificationStatus(VerificationStatus.FLAGGED);
    }

    public List<Listing> browseVerifiedByHost(Long hostId) {
        return listingRepository.findByHostIdAndVerificationStatus(hostId, VerificationStatus.VERIFIED).stream()
                .filter(l -> !Boolean.TRUE.equals(l.getDeleted()) && !Boolean.TRUE.equals(l.getFilled()))
                .toList();
    }

    public Listing getById(Long listingId) {
        Listing l = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        if (Boolean.TRUE.equals(l.getDeleted())) {
            throw new IllegalArgumentException("This listing is no longer available");
        }
        return l;
    }



    public List<Listing> pendingAndFlaggedQueue() {
        List<Listing> pending = listingRepository.findByVerificationStatus(VerificationStatus.PENDING);
        List<Listing> flagged = listingRepository.findByVerificationStatus(VerificationStatus.FLAGGED);
        List<Listing> combined = new java.util.ArrayList<>(pending);
        combined.addAll(flagged);
        return combined;
    }

    public Listing adminReview(Long listingId, boolean approve) {
        Listing listing = getById(listingId);
        listing.setVerificationStatus(approve ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED);
        return listingRepository.save(listing);
    }
    public List<ListingInquiryResponse> myInquiries(Long hostId) {
        return listingInquiryRepository.findByListingHostIdOrderByCreatedAtDesc(hostId)
                .stream().map(ListingInquiryResponse::from).toList();
    }

    // Add this method:
    public List<ListingResponse> browseNearby(double lat, double lng, double radiusKm) {
        return listingRepository.findByVerificationStatus(VerificationStatus.VERIFIED).stream()
                .filter(l -> l.getLatitude() != null && l.getLongitude() != null)
                .map(l -> {
                    double distance = haversineKm(lat, lng, l.getLatitude(), l.getLongitude());
                    ListingResponse r = ListingResponse.from(l);
                    r.setDistanceKm(Math.round(distance * 10) / 10.0);
                    return r;
                })
                .filter(r -> r.getDistanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(ListingResponse::getDistanceKm))
                .toList();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}