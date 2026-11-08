package com.roomsetu.repository;

import com.roomsetu.entity.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    List<ShoppingListItem> findByMatchIdOrderByCreatedAtDesc(Long matchId);
}