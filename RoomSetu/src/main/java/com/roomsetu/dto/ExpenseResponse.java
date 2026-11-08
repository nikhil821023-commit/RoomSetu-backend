package com.roomsetu.dto;

import com.roomsetu.entity.Expense;
import lombok.Data;

import java.time.Instant;

@Data
public class ExpenseResponse {
    private Long id;
    private String description;
    private Integer amount;
    private Long paidByUserId;
    private String paidByName;
    private String category;
    private Instant createdAt;

    public static ExpenseResponse from(Expense e) {
        ExpenseResponse r = new ExpenseResponse();
        r.setId(e.getId());
        r.setDescription(e.getDescription());
        r.setAmount(e.getAmount());
        r.setPaidByUserId(e.getPaidBy().getId());
        r.setPaidByName(e.getPaidBy().getName());
        r.setCategory(e.getCategory());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}