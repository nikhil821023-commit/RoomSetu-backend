package com.roomsetu.repository;

import com.roomsetu.entity.Listing;
import com.roomsetu.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByLocalityIgnoreCaseAndVerificationStatus(String locality, VerificationStatus status);
    List<Listing> findByHostId(Long hostId);
    List<Listing> findByVerificationStatus(VerificationStatus status);
    List<Listing> findByHostIdAndVerificationStatus(Long hostId, VerificationStatus status);
    List<Listing> findByLocalityContainingIgnoreCaseAndVerificationStatus(String locality, VerificationStatus status);

    // --- Phase 3 additions, used by DuplicateListingService ---

    List<Listing> findByAddressIgnoreCase(String address);

    @Query("""
        SELECT DISTINCT l FROM Listing l JOIN l.photoUrls p
        WHERE p IN :urls AND l.host.id <> :hostId
        """)
    List<Listing> findByPhotoUrlsInAndHostIdNot(@Param("urls") List<String> urls, @Param("hostId") Long hostId);
}