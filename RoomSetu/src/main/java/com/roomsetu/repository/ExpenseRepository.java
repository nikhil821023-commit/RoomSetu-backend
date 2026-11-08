package com.roomsetu.repository;

import com.roomsetu.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByMatchIdOrderByCreatedAtDesc(Long matchId);
}