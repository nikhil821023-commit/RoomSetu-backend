package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchSummaryResponse {
    private String summary;
    private boolean aiGenerated; // false means the template fallback produced this — shown honestly, not hidden
}