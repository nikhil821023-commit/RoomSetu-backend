package com.roomsetu.dto;

import com.roomsetu.entity.Listing;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ListingResponse {
    private Long id;
    private Long hostId;
    private String hostName;
    private String locality;
    private String address;
    private Integer rent;
    private String leaseType;
    private LocalDate availableFrom;
    private List<String> photoUrls;
    private String verificationStatus;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;      // only set by search when a point is given
    private String description;
    private List<String> features;
    private boolean filled;

    public static ListingResponse from(Listing l) {
        ListingResponse r = new ListingResponse();
        r.setId(l.getId());
        r.setHostId(l.getHost().getId());
        r.setHostName(l.getHost().getName());
        r.setLocality(l.getLocality());
        r.setAddress(l.getAddress());
        r.setRent(l.getRent());
        r.setLeaseType(l.getLeaseType().name());
        r.setAvailableFrom(l.getAvailableFrom());
        r.setPhotoUrls(l.getPhotoUrls());
        r.setVerificationStatus(l.getVerificationStatus().name());
        r.setLatitude(l.getLatitude());
        r.setLongitude(l.getLongitude());
        r.setDescription(l.getDescription());
        r.setFeatures(l.getFeatures().stream().map(Enum::name).sorted().toList());
        r.setFilled(Boolean.TRUE.equals(l.getFilled()));
        return r;
    }
}