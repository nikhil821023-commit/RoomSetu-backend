package com.roomsetu.service;

import com.roomsetu.dto.ConversationResponse;
import com.roomsetu.dto.MessageResponse;
import com.roomsetu.entity.Conversation;
import com.roomsetu.entity.Message;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.MatchStatus;
import com.roomsetu.repository.ConversationRepository;
import com.roomsetu.repository.ListingInquiryRepository;
import com.roomsetu.repository.MatchRepository;
import com.roomsetu.repository.MessageRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BlockService blockService;
    private final MatchRepository matchRepository;
    private final ListingInquiryRepository listingInquiryRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               UserRepository userRepository,
                               BlockService blockService,
                               MatchRepository matchRepository,
                               ListingInquiryRepository listingInquiryRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.blockService = blockService;
        this.matchRepository = matchRepository;
        this.listingInquiryRepository = listingInquiryRepository;
    }

    private boolean hasConnection(Long a, Long b) {
        boolean acceptedMatch = matchRepository.findBetween(a, b)
                .filter(m -> m.getStatus() == MatchStatus.ACCEPTED)
                .isPresent();
        return acceptedMatch || listingInquiryRepository.existsBetween(a, b);
    }

    private boolean isUserA(Conversation c, Long callerId) {
        return c.getUserA().getId().equals(callerId);
    }

    private Long otherParticipant(Conversation c, Long callerId) {
        return isUserA(c, callerId) ? c.getUserB().getId() : c.getUserA().getId();
    }

    private void requireParticipant(Conversation c, Long callerId) {
        boolean isParticipant = c.getUserA().getId().equals(callerId) || c.getUserB().getId().equals(callerId);
        if (!isParticipant) {
            throw new IllegalArgumentException("You're not part of this conversation");
        }
    }

    private void markRead(Conversation c, Long callerId) {
        Instant now = Instant.now();
        if (isUserA(c, callerId)) c.setUserALastReadAt(now);
        else c.setUserBLastReadAt(now);
        conversationRepository.save(c);
    }

    private long unreadFor(Conversation c, Long callerId) {
        Instant lastRead = isUserA(c, callerId) ? c.getUserALastReadAt() : c.getUserBLastReadAt();
        return messageRepository.countUnread(c.getId(), callerId, lastRead != null ? lastRead : Instant.EPOCH);
    }

    /** When this person last deleted the chat, or null. Anything older is hidden from them. */
    private Instant clearedAt(Conversation c, Long callerId) {
        return isUserA(c, callerId) ? c.getUserAClearedAt() : c.getUserBClearedAt();
    }

    public Conversation getOrCreate(Long callerId, Long otherUserId) {
        if (callerId.equals(otherUserId)) {
            throw new IllegalArgumentException("You can't start a conversation with yourself");
        }
        blockService.requireNotBlocked(callerId, otherUserId);

        Optional<Conversation> existing = conversationRepository.findBetween(callerId, otherUserId);
        if (existing.isPresent()) return existing.get();

        if (!hasConnection(callerId, otherUserId)) {
            throw new IllegalArgumentException(
                    "You can message someone once a match is accepted, or after you've sent an inquiry about their listing");
        }

        User a = userRepository.findById(callerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User b = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return conversationRepository.save(Conversation.builder().userA(a).userB(b).build());
    }

    /** Fetching a thread's messages counts as reading it. */
    public List<MessageResponse> messages(Long conversationId, Long callerId) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        requireParticipant(c, callerId);
        markRead(c, callerId);

        Instant cleared = clearedAt(c, callerId);
        List<Message> messages = cleared == null
                ? messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                : messageRepository.findByConversationIdAndSentAtAfterOrderBySentAtAsc(conversationId, cleared);
        return messages.stream().map(MessageResponse::from).toList();
    }

    public Message send(Long conversationId, Long callerId, String content) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        requireParticipant(c, callerId);
        blockService.requireNotBlocked(callerId, otherParticipant(c, callerId));

        User sender = userRepository.getReferenceById(callerId);
        return messageRepository.save(Message.builder().conversation(c).sender(sender).content(content).build());
    }

    /** Delete for me only. The other person keeps their copy. */
    public void clear(Long conversationId, Long callerId) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        requireParticipant(c, callerId);

        Instant now = Instant.now();
        if (isUserA(c, callerId)) {
            c.setUserAClearedAt(now);
            c.setUserALastReadAt(now);
        } else {
            c.setUserBClearedAt(now);
            c.setUserBLastReadAt(now);
        }
        conversationRepository.save(c);
    }

    public List<ConversationResponse> myConversations(Long callerId) {
        List<ConversationResponse> result = new ArrayList<>();

        for (Conversation c : conversationRepository.findAllForUser(callerId)) {
            if (blockService.isBlockedEitherDirection(callerId, otherParticipant(c, callerId))) continue;

            Instant cleared = clearedAt(c, callerId);
            Message last = cleared == null
                    ? messageRepository.findTopByConversationIdOrderBySentAtDesc(c.getId()).orElse(null)
                    : messageRepository.findTopByConversationIdAndSentAtAfterOrderBySentAtDesc(c.getId(), cleared).orElse(null);

            // Deleted, and nothing new since: stay hidden until they write again.
            if (cleared != null && last == null) continue;

            result.add(ConversationResponse.from(c, callerId, last, unreadFor(c, callerId)));
        }

        result.sort(Comparator.comparing(ConversationResponse::getLastMessageAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    public long unreadTotal(Long callerId) {
        return conversationRepository.findAllForUser(callerId).stream()
                .filter(c -> !blockService.isBlockedEitherDirection(callerId, otherParticipant(c, callerId)))
                .mapToLong(c -> unreadFor(c, callerId))
                .sum();
    }
}