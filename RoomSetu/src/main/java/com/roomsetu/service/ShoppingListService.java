package com.roomsetu.service;

import com.roomsetu.dto.ShoppingListItemRequest;
import com.roomsetu.entity.Match;
import com.roomsetu.entity.ShoppingListItem;
import com.roomsetu.entity.User;
import com.roomsetu.repository.ShoppingListItemRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoppingListService {

    private final ShoppingListItemRepository shoppingListItemRepository;
    private final UserRepository userRepository;
    private final HouseholdAccessService householdAccessService;

    public ShoppingListService(ShoppingListItemRepository shoppingListItemRepository,
                               UserRepository userRepository,
                               HouseholdAccessService householdAccessService) {
        this.shoppingListItemRepository = shoppingListItemRepository;
        this.userRepository = userRepository;
        this.householdAccessService = householdAccessService;
    }

    public ShoppingListItem add(Long matchId, Long callerId, ShoppingListItemRequest req) {
        Match match = householdAccessService.requireActiveHousehold(matchId, callerId);
        User adder = userRepository.getReferenceById(callerId);

        ShoppingListItem item = ShoppingListItem.builder()
                .match(match)
                .item(req.getItem())
                .addedBy(adder)
                .purchased(false)
                .build();

        return shoppingListItemRepository.save(item);
    }

    public List<ShoppingListItem> list(Long matchId, Long callerId) {
        householdAccessService.requireActiveHousehold(matchId, callerId);
        return shoppingListItemRepository.findByMatchIdOrderByCreatedAtDesc(matchId);
    }

    public ShoppingListItem togglePurchased(Long itemId, Long callerId) {
        ShoppingListItem item = shoppingListItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Shopping list item not found"));

        householdAccessService.requireActiveHousehold(item.getMatch().getId(), callerId);

        item.setPurchased(!item.getPurchased());
        return shoppingListItemRepository.save(item);
    }
}