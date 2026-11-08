package com.roomsetu.service;

import com.roomsetu.dto.ListingResponse;
import com.roomsetu.entity.Listing;
import com.roomsetu.entity.enums.LeaseType;
import com.roomsetu.entity.enums.ListingFeature;
import com.roomsetu.entity.enums.VerificationStatus;
import com.roomsetu.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Loads every live listing and filters in memory. Fine for hundreds of listings.
 * Move the filtering into SQL (and PostGIS for distance) before it reaches thousands.
 */
@Service
public class ListingSearchService {

    public record Criteria(Double lat, Double lng, Double radiusKm,
                           Integer minRent, Integer maxRent,
                           LeaseType leaseType, LocalDate availableBy,
                           Set<ListingFeature> features, String locality,
                           String sort, int page, int size) {}

    public record SearchPage(List<ListingResponse> items, int page, int size, long total, boolean hasMore) {}

    private final ListingRepository listingRepository;

    public ListingSearchService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public SearchPage search(Criteria c) {
        boolean hasPoint = c.lat() != null && c.lng() != null;
        List<ListingResponse> matches = new ArrayList<>();

        for (Listing l : listingRepository.findByVerificationStatus(VerificationStatus.VERIFIED)) {
            if (Boolean.TRUE.equals(l.getDeleted()) || Boolean.TRUE.equals(l.getFilled())) continue;
            if (c.minRent() != null && l.getRent() < c.minRent()) continue;
            if (c.maxRent() != null && l.getRent() > c.maxRent()) continue;
            if (c.leaseType() != null && l.getLeaseType() != c.leaseType()) continue;
            if (c.availableBy() != null && l.getAvailableFrom().isAfter(c.availableBy())) continue;
            if (!c.features().isEmpty() && !l.getFeatures().containsAll(c.features())) continue;
            if (!hasPoint && c.locality() != null && !c.locality().isBlank()
                    && !l.getLocality().toLowerCase().contains(c.locality().trim().toLowerCase())) continue;

            Double distance = null;
            if (hasPoint && l.getLatitude() != null && l.getLongitude() != null) {
                distance = Math.round(haversineKm(c.lat(), c.lng(), l.getLatitude(), l.getLongitude()) * 10) / 10.0;
            }
            // A radius filter needs a distance. Listings with no map pin only appear when no radius is set.
            if (hasPoint && c.radiusKm() != null && (distance == null || distance > c.radiusKm())) continue;

            ListingResponse r = ListingResponse.from(l);
            r.setDistanceKm(distance);
            matches.add(r);
        }

        Comparator<ListingResponse> order = switch (c.sort() == null ? "" : c.sort()) {
            case "rent" -> Comparator.comparing(ListingResponse::getRent);
            case "distance" -> Comparator.comparing(ListingResponse::getDistanceKm,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(ListingResponse::getId).reversed();   // newest first
        };
        matches.sort(order);

        int size = Math.max(1, Math.min(c.size(), 30));
        int page = Math.max(0, c.page());
        int from = Math.min(page * size, matches.size());
        int to = Math.min(from + size, matches.size());

        return new SearchPage(new ArrayList<>(matches.subList(from, to)), page, size, matches.size(), to < matches.size());
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}