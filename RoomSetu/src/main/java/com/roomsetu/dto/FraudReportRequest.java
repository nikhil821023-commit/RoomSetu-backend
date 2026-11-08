package com.roomsetu.dto;

import com.roomsetu.entity.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FraudReportRequest {
    @NotNull
    private ReportTargetType targetType;

    @NotNull
    private Long targetId;

    @NotBlank
    private String reason;
}