package com.roomsetu.repository;

import com.roomsetu.entity.ListingInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListingInquiryRepository extends JpaRepository<ListingInquiry, Long> {
    long countByListingId(Long listingId);
    List<ListingInquiry> findByListingIdOrderByCreatedAtDesc(Long listingId);

    // Used by HostDashboardService to pull every lead across all of a host's listings in one query
    List<ListingInquiry> findByListingHostIdOrderByCreatedAtDesc(Long hostId);

    @Query("SELECT COUNT(i) > 0 FROM ListingInquiry i " +
            "WHERE (i.inquirer.id = :a AND i.listing.host.id = :b) " +
            "   OR (i.inquirer.id = :b AND i.listing.host.id = :a)")
    boolean existsBetween(@Param("a") Long a, @Param("b") Long b);
}