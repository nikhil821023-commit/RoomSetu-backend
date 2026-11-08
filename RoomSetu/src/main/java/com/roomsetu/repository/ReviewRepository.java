package com.roomsetu.repository;

import com.roomsetu.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :userId")
    Double averageRatingFor(@Param("userId") Long userId);

    long countByRevieweeId(Long revieweeId);

    boolean existsByReviewerIdAndRevieweeIdAndMatchId(Long reviewerId, Long revieweeId, Long matchId);
}