package com.roomsetu.dto;

import com.roomsetu.entity.enums.ReminderType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReminderRequest {
    @NotNull
    private ReminderType type;

    @NotBlank
    private String title;

    @NotNull @FutureOrPresent
    private LocalDate dueDate;

    private Boolean recurringMonthly;
}