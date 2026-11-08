package com.roomsetu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChecklistItemRequest {
    @NotBlank
    private String title;

    private String note;
}