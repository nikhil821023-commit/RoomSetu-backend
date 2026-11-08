package com.roomsetu.service;

import com.roomsetu.dto.ListingUpdateRequest;
import com.roomsetu.entity.Listing;
import com.roomsetu.repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingManagementService {

    private final ListingRepository listingRepository;
    private final DuplicateListingService duplicateListingService;

    public ListingManagementService(ListingRepository listingRepository,
                                    DuplicateListingService duplicateListingService) {
        this.listingRepository = listingRepository;
        this.duplicateListingService = duplicateListingService;
    }

    private Listing ownedBy(Long hostId, Long listingId) {
        Listing l = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        if (Boolean.TRUE.equals(l.getDeleted())) {
            throw new IllegalArgumentException("Listing not found");
        }
        if (!l.getHost().getId().equals(hostId)) {
            throw new IllegalArgumentException("You can only change your own listings");
        }
        return l;
    }

    @Transactional
    public Listing update(Long hostId, Long listingId, ListingUpdateRequest req) {
        Listing l = ownedBy(hostId, listingId);

        l.setLocality(req.getLocality());
        l.setAddress(req.getAddress());
        l.setRent(req.getRent());
        l.setLeaseType(req.getLeaseType());
        l.setAvailableFrom(req.getAvailableFrom());
        l.setDescription(req.getDescription());
        l.setLatitude(req.getLatitude());
        l.setLongitude(req.getLongitude());

        if (req.getPhotoUrls() != null) {
            l.getPhotoUrls().clear();
            l.getPhotoUrls().addAll(req.getPhotoUrls());
        }
        if (req.getFeatures() != null) {
            l.getFeatures().clear();
            l.getFeatures().addAll(req.getFeatures());
        }

        // Same rule as publishing: an edit can't be used to take an address or photos that
        // belong to another host. The exception rolls the whole change back.
        var dupeCheck = duplicateListingService.check(l);
        if (dupeCheck.suspicious()) {
            throw new IllegalArgumentException(dupeCheck.reason());
        }
        return listingRepository.save(l);
    }

    /** Rented listings are hidden from search but kept, so they can be reopened. */
    @Transactional
    public Listing setFilled(Long hostId, Long listingId, boolean filled) {
        Listing l = ownedBy(hostId, listingId);
        l.setFilled(filled);
        return listingRepository.save(l);
    }

    /** Soft delete: hidden everywhere, but inquiry history and analytics keep working. */
    @Transactional
    public void delete(Long hostId, Long listingId) {
        Listing l = ownedBy(hostId, listingId);
        l.setDeleted(true);
        listingRepository.save(l);
    }
}