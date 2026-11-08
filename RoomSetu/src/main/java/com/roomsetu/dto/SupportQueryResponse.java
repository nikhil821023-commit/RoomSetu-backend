package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupportQueryResponse {
    private boolean matched;
    private String answer;       // null if matched == false
    private String matchedQuestion; // null if matched == false — shown so the user can confirm relevance
}