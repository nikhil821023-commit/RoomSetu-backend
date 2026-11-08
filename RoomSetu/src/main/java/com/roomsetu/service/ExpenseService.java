package com.roomsetu.service;

import com.roomsetu.dto.ExpenseBalanceResponse;
import com.roomsetu.dto.ExpenseRequest;
import com.roomsetu.entity.Expense;
import com.roomsetu.entity.Match;
import com.roomsetu.entity.User;
import com.roomsetu.repository.ExpenseRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final HouseholdAccessService householdAccessService;

    public ExpenseService(ExpenseRepository expenseRepository,
                          UserRepository userRepository,
                          HouseholdAccessService householdAccessService) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.householdAccessService = householdAccessService;
    }

    public Expense add(Long matchId, Long callerId, ExpenseRequest req) {
        Match match = householdAccessService.requireActiveHousehold(matchId, callerId);
        User payer = userRepository.getReferenceById(callerId);

        Expense expense = Expense.builder()
                .match(match)
                .description(req.getDescription())
                .amount(req.getAmount())
                .paidBy(payer)
                .category(req.getCategory())
                .build();

        return expenseRepository.save(expense);
    }

    public List<Expense> list(Long matchId, Long callerId) {
        householdAccessService.requireActiveHousehold(matchId, callerId);
        return expenseRepository.findByMatchIdOrderByCreatedAtDesc(matchId);
    }

    /**
     * Simple running balance across every 50/50-split expense: sum what each person
     * paid, the gap between the two totals halved is what one owes the other.
     * Deliberately recomputed from scratch each call rather than cached — expense
     * volume per household is small enough (dozens, not thousands) that this is cheap,
     * and it avoids an entire class of "balance drifted out of sync" bugs.
     */
    public ExpenseBalanceResponse balance(Long matchId, Long callerId) {
        Match match = householdAccessService.requireActiveHousehold(matchId, callerId);
        Long otherPersonId = householdAccessService.theOtherPerson(match, callerId);

        List<Expense> expenses = expenseRepository.findByMatchIdOrderByCreatedAtDesc(matchId);

        int callerPaid = expenses.stream()
                .filter(e -> e.getPaidBy().getId().equals(callerId))
                .mapToInt(Expense::getAmount).sum();
        int otherPaid = expenses.stream()
                .filter(e -> e.getPaidBy().getId().equals(otherPersonId))
                .mapToInt(Expense::getAmount).sum();

        int totalSpent = callerPaid + otherPaid;
        int diff = callerPaid - otherPaid; // positive => caller has paid more than their share
        int owedShare = Math.abs(diff) / 2;

        User caller = userRepository.getReferenceById(callerId);
        User otherUser = userRepository.getReferenceById(otherPersonId);

        if (diff >= 0) {
            // caller paid more (or exactly equal) — the other person owes the caller
            return new ExpenseBalanceResponse(totalSpent, otherPersonId, otherUser.getName(),
                    callerId, caller.getName(), owedShare);
        } else {
            return new ExpenseBalanceResponse(totalSpent, callerId, caller.getName(),
                    otherPersonId, otherUser.getName(), owedShare);
        }
    }
}