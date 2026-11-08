package com.roomsetu.service;

import com.roomsetu.ai.AiTextClient;
import com.roomsetu.dto.MatchSummaryResponse;
import com.roomsetu.entity.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Product doc Section 10: turns a compatibility score into a sentence instead of
 * a bare percentage. Reuses CompatibilityService (Phase 2) for the actual scoring —
 * this service only handles turning that data into readable text.
 */
@Service
public class MatchSummaryService {

    private static final String SYSTEM_PROMPT = """
        You write a single short, warm sentence (max 25 words) summarizing why two
        prospective roommates in India are compatible, based on the shared traits given.
        No preamble, no quotation marks, just the sentence itself. Do not invent facts
        beyond what's given.
        """;

    private final AiTextClient aiTextClient;
    private final CompatibilityService compatibilityService;

    public MatchSummaryService(AiTextClient aiTextClient, CompatibilityService compatibilityService) {
        this.aiTextClient = aiTextClient;
        this.compatibilityService = compatibilityService;
    }

    public MatchSummaryResponse summarize(Profile a, Profile b) {
        CompatibilityService.CompatibilityResult result = compatibilityService.score(a, b);

        String sharedTraitsPrompt = describeSharedTraits(a, b, result.breakdown());

        Optional<String> aiSummary = aiTextClient.generate(SYSTEM_PROMPT, sharedTraitsPrompt);
        if (aiSummary.isPresent() && !aiSummary.get().isBlank()) {
            return new MatchSummaryResponse(aiSummary.get(), true);
        }

        return new MatchSummaryResponse(templateSummary(a, b, result.breakdown()), false);
    }

    private String describeSharedTraits(Profile a, Profile b, Map<String, Integer> breakdown) {
        StringBuilder sb = new StringBuilder("Shared traits and compatibility scores:\n");
        breakdown.forEach((trait, pct) -> sb.append("- ").append(trait).append(": ").append(pct).append("%\n"));
        if (a.getSleepSchedule() != null) sb.append("Sleep schedule: ").append(a.getSleepSchedule()).append(" vs ").append(b.getSleepSchedule()).append("\n");
        if (a.getFoodPreference() != null) sb.append("Food: ").append(a.getFoodPreference()).append(" vs ").append(b.getFoodPreference()).append("\n");
        return sb.toString();
    }

    /** Always available, zero external dependency — the guaranteed baseline experience. */
    private String templateSummary(Profile a, Profile b, Map<String, Integer> breakdown) {
        StringBuilder sb = new StringBuilder("You both ");
        boolean any = false;

        if (a.getSleepSchedule() != null && a.getSleepSchedule() == b.getSleepSchedule()) {
            sb.append("keep the same sleep schedule");
            any = true;
        }
        if (a.getFoodPreference() != null && a.getFoodPreference() == b.getFoodPreference()) {
            sb.append(any ? " and are " : "are ").append(b.getFoodPreference().name().toLowerCase().replace('_', ' '));
            any = true;
        }
        if (a.getCleanliness() != null && a.getCleanliness() == b.getCleanliness()) {
            sb.append(any ? " with a similar approach to cleaning" : "share a similar approach to cleaning");
            any = true;
        }

        if (!any) {
            int topScore = breakdown.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            return "Your strongest match is on " + topScore + "% compatibility across shared lifestyle signals.";
        }
        sb.append(".");
        return sb.toString();
    }
}