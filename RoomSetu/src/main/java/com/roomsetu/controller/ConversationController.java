package com.roomsetu.controller;

import com.roomsetu.dto.ConversationResponse;
import com.roomsetu.dto.MessageRequest;
import com.roomsetu.dto.MessageResponse;
import com.roomsetu.entity.Conversation;
import com.roomsetu.entity.Message;
import com.roomsetu.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/with/{otherUserId}")
    public ResponseEntity<Map<String, Long>> getOrCreate(@AuthenticationPrincipal Long callerId,
                                                         @PathVariable Long otherUserId) {
        Conversation c = conversationService.getOrCreate(callerId, otherUserId);
        return ResponseEntity.ok(Map.of("conversationId", c.getId()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ConversationResponse>> mine(@AuthenticationPrincipal Long callerId) {
        return ResponseEntity.ok(conversationService.myConversations(callerId));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponse>> messages(@AuthenticationPrincipal Long callerId,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(conversationService.messages(id, callerId));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponse> send(@AuthenticationPrincipal Long callerId,
                                                @PathVariable Long id,
                                                @Valid @RequestBody MessageRequest request) {
        Message m = conversationService.send(id, callerId, request.getContent());
        return ResponseEntity.ok(MessageResponse.from(m));
    }


    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal Long callerId) {
        return ResponseEntity.ok(Map.of("count", conversationService.unreadTotal(callerId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long callerId, @PathVariable Long id) {
        conversationService.clear(id, callerId);
        return ResponseEntity.noContent().build();
    }
}