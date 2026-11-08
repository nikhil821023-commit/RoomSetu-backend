package com.roomsetu.service;

import com.roomsetu.dto.ReviewRequest;
import com.roomsetu.entity.Match;
import com.roomsetu.entity.Review;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.MatchStatus;
import com.roomsetu.repository.MatchRepository;
import com.roomsetu.repository.ReviewRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
                         MatchRepository matchRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
    }

    public Review submit(Long reviewerId, ReviewRequest req) {
        if (reviewerId.equals(req.getRevieweeId())) {
            throw new IllegalArgumentException("You can't review yourself");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));
        User reviewee = userRepository.findById(req.getRevieweeId())
                .orElseThrow(() -> new IllegalArgumentException("Reviewee not found"));

        Match match = matchRepository.findBetween(reviewerId, req.getRevieweeId())
                .filter(m -> m.getStatus() == MatchStatus.ACCEPTED)
                .orElseThrow(() -> new IllegalArgumentException("You can only review someone you've matched with"));

        if (reviewRepository.existsByReviewerIdAndRevieweeIdAndMatchId(reviewerId, req.getRevieweeId(), match.getId())) {
            throw new IllegalArgumentException("You've already reviewed this person");
        }

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .match(match)
                .rating(req.getRating())
                .comment(req.getComment())
                .tenancyPeriod(req.getTenancyPeriod())
                .build();

        return reviewRepository.save(review);
    }

    public List<Review> forUser(Long userId) {
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId);
    }
}