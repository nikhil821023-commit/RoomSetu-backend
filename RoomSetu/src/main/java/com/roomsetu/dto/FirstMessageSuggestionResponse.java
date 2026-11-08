package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FirstMessageSuggestionResponse {
    private String message;
    private boolean aiGenerated;
}