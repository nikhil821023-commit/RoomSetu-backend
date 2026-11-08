package com.roomsetu.controller;

import com.roomsetu.dto.ReminderRequest;
import com.roomsetu.dto.ReminderResponse;
import com.roomsetu.entity.Reminder;
import com.roomsetu.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/household/{matchId}/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> add(@AuthenticationPrincipal Long callerId,
                                                @PathVariable Long matchId,
                                                @Valid @RequestBody ReminderRequest request) {
        Reminder reminder = reminderService.add(matchId, callerId, request);
        return ResponseEntity.ok(ReminderResponse.from(reminder));
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> list(@AuthenticationPrincipal Long callerId,
                                                       @PathVariable Long matchId) {
        List<ReminderResponse> results = reminderService.list(matchId, callerId)
                .stream().map(ReminderResponse::from).toList();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{reminderId}/acknowledge")
    public ResponseEntity<ReminderResponse> acknowledge(@AuthenticationPrincipal Long callerId,
                                                        @PathVariable Long matchId,
                                                        @PathVariable Long reminderId) {
        Reminder reminder = reminderService.acknowledge(reminderId, callerId);
        return ResponseEntity.ok(ReminderResponse.from(reminder));
    }
}