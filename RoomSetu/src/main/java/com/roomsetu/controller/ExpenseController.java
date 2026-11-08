package com.roomsetu.controller;

import com.roomsetu.dto.ExpenseBalanceResponse;
import com.roomsetu.dto.ExpenseRequest;
import com.roomsetu.dto.ExpenseResponse;
import com.roomsetu.entity.Expense;
import com.roomsetu.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/household/{matchId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> add(@AuthenticationPrincipal Long callerId,
                                               @PathVariable Long matchId,
                                               @Valid @RequestBody ExpenseRequest request) {
        Expense expense = expenseService.add(matchId, callerId, request);
        return ResponseEntity.ok(ExpenseResponse.from(expense));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(@AuthenticationPrincipal Long callerId,
                                                      @PathVariable Long matchId) {
        List<ExpenseResponse> results = expenseService.list(matchId, callerId)
                .stream().map(ExpenseResponse::from).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/balance")
    public ResponseEntity<ExpenseBalanceResponse> balance(@AuthenticationPrincipal Long callerId,
                                                          @PathVariable Long matchId) {
        return ResponseEntity.ok(expenseService.balance(matchId, callerId));
    }
}