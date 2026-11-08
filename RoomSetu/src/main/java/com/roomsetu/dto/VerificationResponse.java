package com.roomsetu.dto;

import com.roomsetu.entity.Verification;
import lombok.Data;

import java.time.Instant;

@Data
public class VerificationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String docType;
    private String docUrl;
    private String reviewStatus;
    private Instant submittedAt;

    public static VerificationResponse from(Verification v) {
        VerificationResponse r = new VerificationResponse();
        r.setId(v.getId());
        r.setUserId(v.getUser().getId());
        r.setUserName(v.getUser().getName());
        r.setDocType(v.getDocType());
        r.setDocUrl(v.getDocUrl());
        r.setReviewStatus(v.getReviewStatus().name());
        r.setSubmittedAt(v.getSubmittedAt());
        return r;
    }
}