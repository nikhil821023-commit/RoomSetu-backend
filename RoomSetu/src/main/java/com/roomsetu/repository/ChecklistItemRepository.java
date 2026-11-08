package com.roomsetu.repository;

import com.roomsetu.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    List<ChecklistItem> findByMatchIdOrderByCreatedAtAsc(Long matchId);
}