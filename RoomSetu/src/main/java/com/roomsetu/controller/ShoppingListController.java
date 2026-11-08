package com.roomsetu.controller;

import com.roomsetu.dto.ShoppingListItemRequest;
import com.roomsetu.dto.ShoppingListItemResponse;
import com.roomsetu.entity.ShoppingListItem;
import com.roomsetu.service.ShoppingListService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/household/{matchId}/shopping-list")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @PostMapping
    public ResponseEntity<ShoppingListItemResponse> add(@AuthenticationPrincipal Long callerId,
                                                        @PathVariable Long matchId,
                                                        @Valid @RequestBody ShoppingListItemRequest request) {
        ShoppingListItem item = shoppingListService.add(matchId, callerId, request);
        return ResponseEntity.ok(ShoppingListItemResponse.from(item));
    }

    @GetMapping
    public ResponseEntity<List<ShoppingListItemResponse>> list(@AuthenticationPrincipal Long callerId,
                                                               @PathVariable Long matchId) {
        List<ShoppingListItemResponse> results = shoppingListService.list(matchId, callerId)
                .stream().map(ShoppingListItemResponse::from).toList();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{itemId}/toggle")
    public ResponseEntity<ShoppingListItemResponse> togglePurchased(@AuthenticationPrincipal Long callerId,
                                                                    @PathVariable Long matchId,
                                                                    @PathVariable Long itemId) {
        ShoppingListItem item = shoppingListService.togglePurchased(itemId, callerId);
        return ResponseEntity.ok(ShoppingListItemResponse.from(item));
    }
}