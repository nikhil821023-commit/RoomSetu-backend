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
import com.roomsetu.util.Geo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CompatibilityService compatibilityService;
    private final BlockService blockService;
    private record Scored(DiscoveryCardResponse card, Double km) {}



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
        return discover(currentUserId, 10);
    }

    public List<DiscoveryCardResponse> discover(Long currentUserId, double radiusKm) {
        Profile myProfile = profileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Complete your profile before browsing matches"));

        boolean iHavePin = myProfile.getLatitude() != null && myProfile.getLongitude() != null;
        double radius = Math.max(1, Math.min(radiusKm, 100));
        List<Long> alreadyMatchedIds = matchRepository.findMatchedUserIds(currentUserId);

        List<Scored> scored = new ArrayList<>();
        for (Profile p : profileRepository.findAll()) {
            Long otherId = p.getUser().getId();
            if (otherId.equals(currentUserId) || alreadyMatchedIds.contains(otherId)) continue;
            if (blockService.isBlockedEitherDirection(currentUserId, otherId)) continue;

            Double km = null;
            if (iHavePin && p.getLatitude() != null && p.getLongitude() != null) {
                km = Geo.haversineKm(myProfile.getLatitude(), myProfile.getLongitude(),
                        p.getLatitude(), p.getLongitude());
            }

            // In if both are pinned and close, or they typed the same locality (the old rule).
            boolean nearby = km != null && km <= radius;
            boolean sameLocality = p.getLocality() != null
                    && p.getLocality().equalsIgnoreCase(myProfile.getLocality());
            if (!nearby && !sameLocality) continue;

            DiscoveryCardResponse card = toCard(myProfile, p);
            card.setDistanceLabel(km == null ? null : Geo.distanceLabel(km));
            scored.add(new Scored(card, km));
        }

        // Best compatibility first. Among equals, nearer first.
        scored.sort(Comparator
                .comparing((Scored s) -> s.card().getCompatibilityScore()).reversed()
                .thenComparing(s -> s.km(), Comparator.nullsLast(Comparator.naturalOrder())));

        return scored.stream().map(Scored::card).toList();
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