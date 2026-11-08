package com.roomsetu.dto;

import com.roomsetu.entity.Conversation;
import com.roomsetu.entity.Message;
import com.roomsetu.entity.User;
import lombok.Data;

import java.time.Instant;

@Data
public class ConversationResponse {
    private Long id;
    private Long otherUserId;
    private String otherUserName;
    private String lastMessage;
    private Instant lastMessageAt;
    private long unreadCount;

    public static ConversationResponse from(Conversation c, Long callerId, Message lastMsg, long unread) {
        ConversationResponse r = new ConversationResponse();
        r.setId(c.getId());
        User other = c.getUserA().getId().equals(callerId) ? c.getUserB() : c.getUserA();
        r.setOtherUserId(other.getId());
        r.setOtherUserName(other.getName());
        if (lastMsg != null) {
            r.setLastMessage(lastMsg.getContent());
            r.setLastMessageAt(lastMsg.getSentAt());
        }
        r.setUnreadCount(unread);
        return r;
    }
}