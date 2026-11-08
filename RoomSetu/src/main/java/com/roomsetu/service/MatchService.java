package com.roomsetu.service;

import com.roomsetu.dto.DiscoveryCardResponse;
import com.roomsetu.entity.Match;
import com.roomsetu.entity.Profile;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.MatchStatus;
import com.roomsetu.repository.MatchRepository;
import com.roomsetu.repository.ProfileRepository;
import com.roomsetu.repository.UserRepository;
import com.roomsetu.util.EmailDomains;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CompatibilityService compatibilityService;
    private final BlockService blockService; // Phase 3


    public MatchService(MatchRepository matchRepository,
                        ProfileRepository profileRepository,
                        UserRepository userRepository,
                        CompatibilityService compatibilityService,
                        BlockService blockService) {
        this.matchRepository = matchRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.compatibilityService = compatibilityService;
        this.blockService = blockService;
    }

    /**
     * Ranked discovery feed: every profile in the same locality, minus people
     * already matched with (in either direction), sorted by compatibility score descending.
     */
    public List<DiscoveryCardResponse> discover(Long currentUserId) {
        Profile myProfile = profileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Complete your profile before browsing matches"));

        List<Long> alreadyMatchedIds = matchRepository.findMatchedUserIds(currentUserId);

        return profileRepository.findAll().stream()
                .filter(p -> !p.getUser().getId().equals(currentUserId))
                .filter(p -> !alreadyMatchedIds.contains(p.getUser().getId()))
                .filter(p -> !blockService.isBlockedEitherDirection(currentUserId, p.getUser().getId())) // Phase 3
                .filter(p -> p.getLocality() != null
                        && p.getLocality().equalsIgnoreCase(myProfile.getLocality()))
                .map(candidate -> toCard(myProfile, candidate))
                .sorted(Comparator.comparing(DiscoveryCardResponse::getCompatibilityScore).reversed())
                .toList();
    }

    private DiscoveryCardResponse toCard(Profile myProfile, Profile candidate) {
        CompatibilityService.CompatibilityResult result = compatibilityService.score(myProfile, candidate);
        User candidateUser = candidate.getUser();

        return DiscoveryCardResponse.builder()
                .userId(candidateUser.getId())
                .name(candidateUser.getName())
                .locality(candidate.getLocality())
                .verificationStatus(candidateUser.getVerificationStatus().name())
                .workEmailDomain(EmailDomains.verifiedDomain(candidateUser))
                .photoUrl(candidate.getPhotoUrl())
                .compatibilityScore(result.totalScore())
                .scoreBreakdown(result.breakdown())
                .bio(candidate.getBio())
                .build();
    }


    public Match requestMatch(Long requesterId, Long targetId) {
        if (requesterId.equals(targetId)) {
            throw new IllegalArgumentException("You can't match with yourself");
        }

        blockService.requireNotBlocked(requesterId, targetId);

        matchRepository.findBetween(requesterId, targetId).ifPresent(existing -> {
            throw new IllegalArgumentException("A match request already exists between these users");
        });

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        Profile requesterProfile = profileRepository.findByUserId(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Complete your profile first"));
        Profile targetProfile = profileRepository.findByUserId(targetId)
                .orElseThrow(() -> new IllegalArgumentException("This user hasn't completed their profile yet"));

        int score = compatibilityService.score(requesterProfile, targetProfile).totalScore();

        Match match = Match.builder()
                .requester(requester)
                .target(target)
                .compatibilityScore(score)
                .status(MatchStatus.PENDING)
                .build();

        return matchRepository.save(match);
    }

    /** Only the target of the request can accept or reject it — not the requester. */
    public Match respond(Long matchId, Long respondingUserId, boolean accept) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match request not found"));

        if (!match.getTarget().getId().equals(respondingUserId)) {
            throw new IllegalArgumentException("Only the recipient of this request can respond to it");
        }

        blockService.requireNotBlocked(match.getRequester().getId(), match.getTarget().getId());

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new IllegalArgumentException("This match request has already been responded to");
        }

        match.setStatus(accept ? MatchStatus.ACCEPTED : MatchStatus.REJECTED);
        match.setRespondedAt(Instant.now());
        return matchRepository.save(match);
    }

    public List<Match> myMatches(Long userId) {
        return matchRepository.findAllForUser(userId);
    }

    public List<Match> incomingRequests(Long userId) {
        return matchRepository.findIncoming(userId, MatchStatus.PENDING);
    }
}