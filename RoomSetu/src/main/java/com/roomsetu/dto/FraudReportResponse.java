package com.roomsetu.dto;

import com.roomsetu.entity.FraudReport;
import lombok.Data;

import java.time.Instant;

@Data
public class FraudReportResponse {
    private Long id;
    private Long reporterId;
    private String targetType;
    private Long targetId;
    private String reason;
    private String status;
    private Instant createdAt;

    public static FraudReportResponse from(FraudReport f) {
        FraudReportResponse d = new FraudReportResponse();
        d.setId(f.getId());
        d.setReporterId(f.getReporter().getId());
        d.setTargetType(f.getTargetType().name());
        d.setTargetId(f.getTargetId());
        d.setReason(f.getReason());
        d.setStatus(f.getStatus().name());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }
}