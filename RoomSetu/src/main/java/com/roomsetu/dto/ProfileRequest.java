package com.roomsetu.dto;

import com.roomsetu.entity.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {
    @NotNull @Positive
    private Integer budgetMin;

    @NotNull @Positive
    private Integer budgetMax;

    @NotBlank
    private String locality;

    private SleepSchedule sleepSchedule;
    private WorkFromHome workFromHome;
    private FoodPreference foodPreference;
    private Cleanliness cleanliness;
    private Boolean smokes;
    private Boolean drinks;
    private Boolean hasPets;
    private Boolean okWithPets;
    private String bio;

    // Add alongside the existing fields:
    private String photoUrl;


    private Double latitude;
    private Double longitude;

    @Size(max = 200)
    private String locationLabel;
}