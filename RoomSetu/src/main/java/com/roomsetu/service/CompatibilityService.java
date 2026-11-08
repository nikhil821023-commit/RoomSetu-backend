package com.roomsetu.service;

import com.roomsetu.entity.Profile;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Weighted rule-based compatibility scoring, per the product doc (Section 14.2):
 * transparent and explainable now; swap in ML once there's real usage data to train on.
 *
 * Weights sum to 100. Adjust freely — they're isolated here on purpose so tuning
 * the algorithm never touches controller or entity code.
 */
@Service
public class CompatibilityService {

    private static final int WEIGHT_SLEEP = 20;
    private static final int WEIGHT_FOOD = 20;
    private static final int WEIGHT_CLEANLINESS = 20;
    private static final int WEIGHT_BUDGET = 25;
    private static final int WEIGHT_WFH = 15;

    public record CompatibilityResult(int totalScore, Map<String, Integer> breakdown) {}

    public CompatibilityResult score(Profile a, Profile b) {
        Map<String, Integer> breakdown = new LinkedHashMap<>();

        int sleepScore = matches(a.getSleepSchedule(), b.getSleepSchedule()) ? WEIGHT_SLEEP : WEIGHT_SLEEP / 4;
        int foodScore = matches(a.getFoodPreference(), b.getFoodPreference()) ? WEIGHT_FOOD : 0;
        int cleanlinessScore = cleanlinessCompatibility(a, b);
        int budgetScore = budgetOverlapScore(a, b);
        int wfhScore = matches(a.getWorkFromHome(), b.getWorkFromHome()) ? WEIGHT_WFH : WEIGHT_WFH / 2;

        breakdown.put("Sleep schedule", scaleToPercent(sleepScore, WEIGHT_SLEEP));
        breakdown.put("Food preference", scaleToPercent(foodScore, WEIGHT_FOOD));
        breakdown.put("Cleanliness", scaleToPercent(cleanlinessScore, WEIGHT_CLEANLINESS));
        breakdown.put("Budget overlap", scaleToPercent(budgetScore, WEIGHT_BUDGET));
        breakdown.put("Work-from-home", scaleToPercent(wfhScore, WEIGHT_WFH));

        int total = sleepScore + foodScore + cleanlinessScore + budgetScore + wfhScore;
        return new CompatibilityResult(total, breakdown);
    }

    private boolean matches(Object x, Object y) {
        return x != null && x.equals(y);
    }

    /** Adjacent cleanliness levels (e.g. TIDY_WEEKLY vs VERY_PARTICULAR) still get partial credit. */
    private int cleanlinessCompatibility(Profile a, Profile b) {
        if (a.getCleanliness() == null || b.getCleanliness() == null) return WEIGHT_CLEANLINESS / 4;
        if (a.getCleanliness() == b.getCleanliness()) return WEIGHT_CLEANLINESS;

        int diff = Math.abs(a.getCleanliness().ordinal() - b.getCleanliness().ordinal());
        return diff == 1 ? WEIGHT_CLEANLINESS / 2 : WEIGHT_CLEANLINESS / 5;
    }

    /** Scores how much two people's budget ranges overlap, not just whether they match exactly. */
    private int budgetOverlapScore(Profile a, Profile b) {
        if (a.getBudgetMin() == null || a.getBudgetMax() == null
                || b.getBudgetMin() == null || b.getBudgetMax() == null) {
            return WEIGHT_BUDGET / 4;
        }

        int overlapStart = Math.max(a.getBudgetMin(), b.getBudgetMin());
        int overlapEnd = Math.min(a.getBudgetMax(), b.getBudgetMax());

        if (overlapEnd < overlapStart) return 0; // no overlap at all

        int overlapSize = overlapEnd - overlapStart;
        int widestRange = Math.max(a.getBudgetMax() - a.getBudgetMin(), b.getBudgetMax() - b.getBudgetMin());
        if (widestRange <= 0) return WEIGHT_BUDGET;

        double ratio = Math.min(1.0, (double) overlapSize / widestRange);
        return (int) Math.round(ratio * WEIGHT_BUDGET);
    }

    private int scaleToPercent(int score, int weight) {
        return (int) Math.round((score / (double) weight) * 100);
    }
}