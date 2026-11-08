package com.roomsetu.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull
    private Long revieweeId;

    private Long matchId; // optional, but strongly recommended — see ReviewService

    @NotNull @Min(1) @Max(5)
    private Integer rating;

    @Size(max = 500)
    private String comment;

    private String tenancyPeriod;
}