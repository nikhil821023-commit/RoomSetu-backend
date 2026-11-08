package com.roomsetu.service;

import com.roomsetu.dto.SupportQueryResponse;
import com.roomsetu.entity.SupportFaqEntry;
import com.roomsetu.repository.SupportFaqEntryRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Plain keyword-overlap matching, not embeddings or an AI call — see the note on
 * SupportFaqEntry for why. A confidence threshold below which the query routes to
 * a human is more important here than getting clever with the matching itself.
 */
@Service
public class FaqAutoResponseService {

    private static final int MIN_MATCHING_KEYWORDS = 2;

    private final SupportFaqEntryRepository supportFaqEntryRepository;

    public FaqAutoResponseService(SupportFaqEntryRepository supportFaqEntryRepository) {
        this.supportFaqEntryRepository = supportFaqEntryRepository;
    }

    public SupportQueryResponse answer(String question) {
        Set<String> queryWords = tokenize(question);

        List<SupportFaqEntry> allEntries = supportFaqEntryRepository.findAll();

        SupportFaqEntry bestMatch = null;
        int bestScore = 0;

        for (SupportFaqEntry entry : allEntries) {
            Set<String> entryKeywords = Arrays.stream(entry.getKeywords().split(","))
                    .map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());

            int overlap = (int) queryWords.stream().filter(entryKeywords::contains).count();
            if (overlap > bestScore) {
                bestScore = overlap;
                bestMatch = entry;
            }
        }

        if (bestMatch != null && bestScore >= MIN_MATCHING_KEYWORDS) {
            return new SupportQueryResponse(true, bestMatch.getAnswer(), bestMatch.getQuestion());
        }

        return new SupportQueryResponse(false,
                "I couldn't find a confident answer to that — routing you to a human. "
                        + "Our team typically responds within a day.",
                null);
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 2) // drop short noise words
                .collect(Collectors.toSet());
    }
}