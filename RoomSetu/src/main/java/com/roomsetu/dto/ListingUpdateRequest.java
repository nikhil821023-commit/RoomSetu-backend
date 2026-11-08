package com.roomsetu.dto;

import com.roomsetu.entity.enums.LeaseType;
import com.roomsetu.entity.enums.ListingFeature;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class ListingUpdateRequest {
    @NotBlank
    private String locality;

    @NotBlank
    private String address;

    @NotNull @Positive
    private Integer rent;

    @NotNull
    private LeaseType leaseType;

    @NotNull
    private LocalDate availableFrom;

    private List<String> photoUrls;

    private Double latitude;
    private Double longitude;

    @Size(max = 500)
    private String description;

    private Set<ListingFeature> features;
}