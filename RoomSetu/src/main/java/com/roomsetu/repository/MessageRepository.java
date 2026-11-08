package com.roomsetu.repository;

import com.roomsetu.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);
    Optional<Message> findTopByConversationIdOrderBySentAtDesc(Long conversationId);

    List<Message> findByConversationIdAndSentAtAfterOrderBySentAtAsc(Long conversationId, Instant after);
    Optional<Message> findTopByConversationIdAndSentAtAfterOrderBySentAtDesc(Long conversationId, Instant after);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :cid AND m.sender.id <> :uid AND m.sentAt > :after")
    long countUnread(@Param("cid") Long cid, @Param("uid") Long uid, @Param("after") Instant after);
}