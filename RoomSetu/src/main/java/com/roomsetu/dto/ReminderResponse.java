package com.roomsetu.dto;

import com.roomsetu.entity.Reminder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReminderResponse {
    private Long id;
    private String type;
    private String title;
    private LocalDate dueDate;
    private Boolean recurringMonthly;
    private Boolean acknowledged;

    public static ReminderResponse from(Reminder r) {
        ReminderResponse d = new ReminderResponse();
        d.setId(r.getId());
        d.setType(r.getType().name());
        d.setTitle(r.getTitle());
        d.setDueDate(r.getDueDate());
        d.setRecurringMonthly(r.getRecurringMonthly());
        d.setAcknowledged(r.getAcknowledged());
        return d;
    }
}