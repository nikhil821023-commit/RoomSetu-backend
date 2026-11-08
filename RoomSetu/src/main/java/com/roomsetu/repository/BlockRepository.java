package com.roomsetu.repository;

import com.roomsetu.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :userId")
    List<Long> findBlockedIds(Long userId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT b.blocker.id FROM Block b WHERE b.blocked.id = :userId")
    List<Long> findBlockedByIds(Long userId);

    List<Block> findByBlockerIdOrderByCreatedAtDesc(Long blockerId);
}