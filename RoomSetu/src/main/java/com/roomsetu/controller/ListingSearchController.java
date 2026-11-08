package com.roomsetu.controller;

import com.roomsetu.entity.enums.LeaseType;
import com.roomsetu.entity.enums.ListingFeature;
import com.roomsetu.service.ListingSearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/listings/public")
public class ListingSearchController {

    private final ListingSearchService searchService;

    public ListingSearchController(ListingSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<ListingSearchService.SearchPage> search(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) Integer minRent,
            @RequestParam(required = false) Integer maxRent,
            @RequestParam(required = false) LeaseType leaseType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableBy,
            @RequestParam(required = false) List<ListingFeature> features,
            @RequestParam(required = false) String locality,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Set<ListingFeature> wanted = features == null ? new HashSet<>() : new HashSet<>(features);
        return ResponseEntity.ok(searchService.search(new ListingSearchService.Criteria(
                lat, lng, radiusKm, minRent, maxRent, leaseType, availableBy, wanted, locality, sort, page, size)));
    }
}