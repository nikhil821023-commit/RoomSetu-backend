package com.roomsetu.dto;

import com.roomsetu.entity.ChecklistItem;
import lombok.Data;

import java.time.Instant;

@Data
public class ChecklistItemResponse {
    private Long id;
    private String title;
    private String note;
    private Boolean done;
    private String createdByName;
    private Instant createdAt;
    private Instant completedAt;

    public static ChecklistItemResponse from(ChecklistItem c) {
        ChecklistItemResponse r = new ChecklistItemResponse();
        r.setId(c.getId());
        r.setTitle(c.getTitle());
        r.setNote(c.getNote());
        r.setDone(c.getDone());
        r.setCreatedByName(c.getCreatedBy().getName());
        r.setCreatedAt(c.getCreatedAt());
        r.setCompletedAt(c.getCompletedAt());
        return r;
    }
}