package com.roomsetu.repository;

import com.roomsetu.entity.ListingView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingViewRepository extends JpaRepository<ListingView, Long> {
    long countByListingId(Long listingId);
}