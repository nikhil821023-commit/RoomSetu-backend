package com.roomsetu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ExpenseRequest {
    @NotBlank
    private String description;

    @NotNull @Positive
    private Integer amount;

    @NotBlank
    private String category;
}