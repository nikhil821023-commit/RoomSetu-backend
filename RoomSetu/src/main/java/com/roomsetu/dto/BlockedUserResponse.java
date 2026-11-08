package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockedUserResponse {
    private Long userId;
    private String name;
}