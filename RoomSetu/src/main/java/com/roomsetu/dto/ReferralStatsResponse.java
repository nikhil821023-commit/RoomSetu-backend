package com.roomsetu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReferralStatsResponse {
    private long totalReferred;
    private long rewarded;     // completed-match referrals — the number that actually counts
    private long pending;      // signed up but haven't completed a match yet
}