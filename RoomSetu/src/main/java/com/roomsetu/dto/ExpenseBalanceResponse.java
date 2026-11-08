package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * "owesUserId owes owedToUserId owesAmount" — settled up (amount == 0) is a valid,
 * common state and should be handled gracefully by the frontend, not treated as an error.
 */
@Data
@AllArgsConstructor
public class ExpenseBalanceResponse {
    private Integer totalSpent;
    private Long owesUserId;
    private String owesUserName;
    private Long owedToUserId;
    private String owedToUserName;
    private Integer owesAmount;
}