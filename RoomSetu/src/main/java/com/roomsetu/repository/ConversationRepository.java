package com.roomsetu.repository;

import com.roomsetu.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
        SELECT c FROM Conversation c
        WHERE (c.userA.id = :u1 AND c.userB.id = :u2)
           OR (c.userA.id = :u2 AND c.userB.id = :u1)
        """)
    Optional<Conversation> findBetween(@Param("u1") Long u1, @Param("u2") Long u2);

    @Query("""
        SELECT c FROM Conversation c
        WHERE c.userA.id = :userId OR c.userB.id = :userId
        ORDER BY c.createdAt DESC
        """)
    List<Conversation> findAllForUser(@Param("userId") Long userId);
}