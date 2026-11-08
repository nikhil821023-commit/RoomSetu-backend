package com.roomsetu.dto;

import com.roomsetu.entity.Review;
import lombok.Data;

import java.time.Instant;

@Data
public class ReviewResponse {
    private Long id;
    private Long reviewerId;
    private String reviewerName;
    private Long revieweeId;
    private Integer rating;
    private String comment;
    private String tenancyPeriod;
    private Instant createdAt;

    public static ReviewResponse from(Review r) {
        ReviewResponse d = new ReviewResponse();
        d.setId(r.getId());
        d.setReviewerId(r.getReviewer().getId());
        d.setReviewerName(r.getReviewer().getName());
        d.setRevieweeId(r.getReviewee().getId());
        d.setRating(r.getRating());
        d.setComment(r.getComment());
        d.setTenancyPeriod(r.getTenancyPeriod());
        d.setCreatedAt(r.getCreatedAt());
        return d;
    }
}