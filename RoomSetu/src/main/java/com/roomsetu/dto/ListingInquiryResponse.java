package com.roomsetu.dto;

import com.roomsetu.entity.ListingInquiry;
import lombok.Data;

import java.time.Instant;

@Data
public class ListingInquiryResponse {
    private Long id;
    private Long listingId;
    private Long inquirerId;
    private String inquirerName;
    private String message;
    private Instant createdAt;
    private Long hostId;
    private String hostName;

    public static ListingInquiryResponse from(ListingInquiry i) {
        ListingInquiryResponse r = new ListingInquiryResponse();
        r.setId(i.getId());
        r.setListingId(i.getListing().getId());
        r.setInquirerId(i.getInquirer().getId());
        r.setInquirerName(i.getInquirer().getName());
        r.setMessage(i.getMessage());
        r.setCreatedAt(i.getCreatedAt());
        r.setHostId(i.getListing().getHost().getId());
        r.setHostName(i.getListing().getHost().getName());
        return r;
    }
}