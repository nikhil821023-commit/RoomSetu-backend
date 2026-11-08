package com.roomsetu.dto;

import com.roomsetu.entity.Message;
import lombok.Data;

import java.time.Instant;

@Data
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String content;
    private Instant sentAt;

    public static MessageResponse from(Message m) {
        MessageResponse r = new MessageResponse();
        r.setId(m.getId());
        r.setSenderId(m.getSender().getId());
        r.setSenderName(m.getSender().getName());
        r.setContent(m.getContent());
        r.setSentAt(m.getSentAt());
        return r;
    }
}