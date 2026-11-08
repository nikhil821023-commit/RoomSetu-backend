package com.roomsetu.controller;

import com.roomsetu.dto.ChecklistItemRequest;
import com.roomsetu.dto.ChecklistItemResponse;
import com.roomsetu.entity.ChecklistItem;
import com.roomsetu.service.ChecklistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/household/{matchId}/checklist")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @PostMapping
    public ResponseEntity<ChecklistItemResponse> add(@AuthenticationPrincipal Long callerId,
                                                     @PathVariable Long matchId,
                                                     @Valid @RequestBody ChecklistItemRequest request) {
        ChecklistItem item = checklistService.add(matchId, callerId, request);
        return ResponseEntity.ok(ChecklistItemResponse.from(item));
    }

    @GetMapping
    public ResponseEntity<List<ChecklistItemResponse>> list(@AuthenticationPrincipal Long callerId,
                                                            @PathVariable Long matchId) {
        List<ChecklistItemResponse> results = checklistService.list(matchId, callerId)
                .stream().map(ChecklistItemResponse::from).toList();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{itemId}/toggle")
    public ResponseEntity<ChecklistItemResponse> toggle(@AuthenticationPrincipal Long callerId,
                                                        @PathVariable Long matchId,
                                                        @PathVariable Long itemId) {
        ChecklistItem item = checklistService.toggle(itemId, callerId);
        return ResponseEntity.ok(ChecklistItemResponse.from(item));
    }
}