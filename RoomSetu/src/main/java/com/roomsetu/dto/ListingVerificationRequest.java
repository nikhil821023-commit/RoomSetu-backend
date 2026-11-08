package com.roomsetu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ListingVerificationRequest {
    @NotBlank
    private String docType;

    @NotBlank
    private String docUrl;
}