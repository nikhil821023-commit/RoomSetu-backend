package com.roomsetu.dto;

import com.roomsetu.entity.Match;
import lombok.Data;

import java.time.Instant;

@Data
public class MatchResponse {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long targetId;
    private String targetName;
    private Integer compatibilityScore;
    private String status;
    private Instant createdAt;

    public static MatchResponse from(Match m) {
        MatchResponse r = new MatchResponse();
        r.setId(m.getId());
        r.setRequesterId(m.getRequester().getId());
        r.setRequesterName(m.getRequester().getName());
        r.setTargetId(m.getTarget().getId());
        r.setTargetName(m.getTarget().getName());
        r.setCompatibilityScore(m.getCompatibilityScore());
        r.setStatus(m.getStatus().name());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}