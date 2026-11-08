package com.roomsetu.controller;

import com.roomsetu.dto.ReviewRequest;
import com.roomsetu.dto.ReviewResponse;
import com.roomsetu.entity.Review;
import com.roomsetu.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> submit(@AuthenticationPrincipal Long reviewerId,
                                                 @Valid @RequestBody ReviewRequest request) {
        Review review = reviewService.submit(reviewerId, request);
        return ResponseEntity.ok(ReviewResponse.from(review));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> forUser(@PathVariable Long userId) {
        List<ReviewResponse> results = reviewService.forUser(userId)
                .stream().map(ReviewResponse::from).toList();
        return ResponseEntity.ok(results);
    }
}