package com.roomsetu.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ListingInquiryRequest {
    @Size(max = 500)
    private String message;
}