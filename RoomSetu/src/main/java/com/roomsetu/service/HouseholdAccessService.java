package com.roomsetu.service;

import com.roomsetu.entity.Match;
import com.roomsetu.entity.enums.MatchStatus;
import com.roomsetu.repository.MatchRepository;
import org.springframework.stereotype.Service;

/**
 * Every Phase 4 service needs the same two checks before touching household data:
 * (1) the match exists and is ACCEPTED, (2) the caller is one of its two people.
 * Centralized here so that check can't drift out of sync across four services.
 */
@Service
public class HouseholdAccessService {

    private final MatchRepository matchRepository;

    public HouseholdAccessService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public Match requireActiveHousehold(Long matchId, Long callerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        if (match.getStatus() != MatchStatus.ACCEPTED) {
            throw new IllegalArgumentException("Move-in tools unlock once a match is accepted");
        }

        boolean isParticipant = match.getRequester().getId().equals(callerId)
                || match.getTarget().getId().equals(callerId);

        if (!isParticipant) {
            throw new IllegalArgumentException("You're not part of this household");
        }

        return match;
    }

    /** The other person in the household — used by ExpenseService to compute who owes whom. */
    public Long theOtherPerson(Match match, Long callerId) {
        return match.getRequester().getId().equals(callerId)
                ? match.getTarget().getId()
                : match.getRequester().getId();
    }
}