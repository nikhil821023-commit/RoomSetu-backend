package com.roomsetu.dto;

import com.roomsetu.entity.enums.PartnerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartnerOrganizationRequest {
    @NotBlank
    private String name;

    @NotNull
    private PartnerType type;

    @NotBlank
    private String locality;
}