package com.roomsetu.repository;

import com.roomsetu.entity.Match;
import com.roomsetu.entity.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByRequesterIdAndTargetId(Long requesterId, Long targetId);

    // A match exists in either direction — needed so A can't request B twice
    // by swapping who's "requester" once B has already requested A.
    @Query("""
        SELECT m FROM Match m
        WHERE (m.requester.id = :userA AND m.target.id = :userB)
           OR (m.requester.id = :userB AND m.target.id = :userA)
        """)
    Optional<Match> findBetween(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query("""
        SELECT m FROM Match m
        WHERE (m.requester.id = :userId OR m.target.id = :userId)
        ORDER BY m.createdAt DESC
        """)
    List<Match> findAllForUser(@Param("userId") Long userId);

    @Query("""
        SELECT m FROM Match m
        WHERE m.target.id = :userId AND m.status = :status
        ORDER BY m.createdAt DESC
        """)
    List<Match> findIncoming(@Param("userId") Long userId, @Param("status") MatchStatus status);

    // Every user ID this person already has any match record with —
    // used to exclude them from the discovery feed so they don't see the same card twice.
    @Query("""
        SELECT CASE WHEN m.requester.id = :userId THEN m.target.id ELSE m.requester.id END
        FROM Match m
        WHERE m.requester.id = :userId OR m.target.id = :userId
        """)
    List<Long> findMatchedUserIds(@Param("userId") Long userId);
}