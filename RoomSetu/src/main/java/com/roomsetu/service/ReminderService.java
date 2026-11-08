package com.roomsetu.service;

import com.roomsetu.dto.ReminderRequest;
import com.roomsetu.entity.Match;
import com.roomsetu.entity.Reminder;
import com.roomsetu.repository.ReminderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final HouseholdAccessService householdAccessService;

    public ReminderService(ReminderRepository reminderRepository,
                           HouseholdAccessService householdAccessService) {
        this.reminderRepository = reminderRepository;
        this.householdAccessService = householdAccessService;
    }

    public Reminder add(Long matchId, Long callerId, ReminderRequest req) {
        Match match = householdAccessService.requireActiveHousehold(matchId, callerId);

        Reminder reminder = Reminder.builder()
                .match(match)
                .type(req.getType())
                .title(req.getTitle())
                .dueDate(req.getDueDate())
                .recurringMonthly(req.getRecurringMonthly() != null && req.getRecurringMonthly())
                .acknowledged(false)
                .build();

        return reminderRepository.save(reminder);
    }

    public List<Reminder> list(Long matchId, Long callerId) {
        householdAccessService.requireActiveHousehold(matchId, callerId);
        return reminderRepository.findByMatchIdOrderByDueDateAsc(matchId);
    }

    public Reminder acknowledge(Long reminderId, Long callerId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));

        householdAccessService.requireActiveHousehold(reminder.getMatch().getId(), callerId);

        reminder.setAcknowledged(true);
        Reminder saved = reminderRepository.save(reminder);

        // A recurring reminder re-creates itself ~30 days out once cleared, so
        // "rent reminder" doesn't need to be manually re-added every month.
        if (Boolean.TRUE.equals(reminder.getRecurringMonthly())) {
            Reminder next = Reminder.builder()
                    .match(reminder.getMatch())
                    .type(reminder.getType())
                    .title(reminder.getTitle())
                    .dueDate(reminder.getDueDate().plusMonths(1))
                    .recurringMonthly(true)
                    .acknowledged(false)
                    .build();
            reminderRepository.save(next);
        }

        return saved;
    }
}