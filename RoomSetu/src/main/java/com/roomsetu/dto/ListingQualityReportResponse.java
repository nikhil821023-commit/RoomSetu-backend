package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ListingQualityReportResponse {
    private int qualityScore; // 0-100, simple rule-based composite — see service
    private List<String> suggestions;
}