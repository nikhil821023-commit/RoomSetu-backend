package com.roomsetu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportQueryRequest {
    @NotBlank
    private String question;
}