package com.roomsetu.service;

import com.roomsetu.dto.ChecklistItemRequest;
import com.roomsetu.entity.ChecklistItem;
import com.roomsetu.entity.Match;
import com.roomsetu.entity.User;
import com.roomsetu.repository.ChecklistItemRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ChecklistService {

    private final ChecklistItemRepository checklistItemRepository;
    private final UserRepository userRepository;
    private final HouseholdAccessService householdAccessService;

    public ChecklistService(ChecklistItemRepository checklistItemRepository,
                            UserRepository userRepository,
                            HouseholdAccessService householdAccessService) {
        this.checklistItemRepository = checklistItemRepository;
        this.userRepository = userRepository;
        this.householdAccessService = householdAccessService;
    }

    public ChecklistItem add(Long matchId, Long callerId, ChecklistItemRequest req) {
        Match match = householdAccessService.requireActiveHousehold(matchId, callerId);
        User creator = userRepository.getReferenceById(callerId);

        ChecklistItem item = ChecklistItem.builder()
                .match(match)
                .title(req.getTitle())
                .note(req.getNote())
                .done(false)
                .createdBy(creator)
                .build();

        return checklistItemRepository.save(item);
    }

    public List<ChecklistItem> list(Long matchId, Long callerId) {
        householdAccessService.requireActiveHousehold(matchId, callerId);
        return checklistItemRepository.findByMatchIdOrderByCreatedAtAsc(matchId);
    }

    public ChecklistItem toggle(Long itemId, Long callerId) {
        ChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Checklist item not found"));

        householdAccessService.requireActiveHousehold(item.getMatch().getId(), callerId);

        boolean newState = !item.getDone();
        item.setDone(newState);
        item.setCompletedAt(newState ? Instant.now() : null);

        return checklistItemRepository.save(item);
    }
}