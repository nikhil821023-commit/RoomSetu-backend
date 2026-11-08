package com.roomsetu.dto;

import com.roomsetu.entity.ShoppingListItem;
import lombok.Data;

@Data
public class ShoppingListItemResponse {
    private Long id;
    private String item;
    private String addedByName;
    private Boolean purchased;

    public static ShoppingListItemResponse from(ShoppingListItem s) {
        ShoppingListItemResponse r = new ShoppingListItemResponse();
        r.setId(s.getId());
        r.setItem(s.getItem());
        r.setAddedByName(s.getAddedBy().getName());
        r.setPurchased(s.getPurchased());
        return r;
    }
}