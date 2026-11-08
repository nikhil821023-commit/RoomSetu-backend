package com.roomsetu.service;

import com.roomsetu.ai.AiTextClient;
import com.roomsetu.dto.FirstMessageSuggestionResponse;
import com.roomsetu.entity.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FirstMessageAssistantService {

    private static final String SYSTEM_PROMPT = """
        You draft a single friendly, casual opening message (max 40 words) from one
        prospective roommate to another, referencing ONE specific shared trait given.
        Sound like a real 20-something Indian renter texting, not a formal introduction.
        No preamble, just the message text.
        """;

    private final AiTextClient aiTextClient;

    public FirstMessageAssistantService(AiTextClient aiTextClient) {
        this.aiTextClient = aiTextClient;
    }

    public FirstMessageSuggestionResponse suggest(Profile me, Profile target) {
        String sharedTrait = findSharedTrait(me, target);
        String prompt = "My profile and their profile share this trait: " + sharedTrait
                + ". Their locality: " + target.getLocality() + ".";

        Optional<String> aiMessage = aiTextClient.generate(SYSTEM_PROMPT, prompt);
        if (aiMessage.isPresent() && !aiMessage.get().isBlank()) {
            return new FirstMessageSuggestionResponse(aiMessage.get(), true);
        }

        return new FirstMessageSuggestionResponse(templateMessage(sharedTrait), false);
    }

    private String findSharedTrait(Profile a, Profile b) {
        if (a.getSleepSchedule() != null && a.getSleepSchedule() == b.getSleepSchedule()) {
            return "same sleep schedule (" + a.getSleepSchedule() + ")";
        }
        if (a.getFoodPreference() != null && a.getFoodPreference() == b.getFoodPreference()) {
            return "same food preference (" + a.getFoodPreference() + ")";
        }
        if (a.getCleanliness() != null && a.getCleanliness() == b.getCleanliness()) {
            return "similar cleanliness habits";
        }
        return "similar budget range";
    }

    private String templateMessage(String sharedTrait) {
        return "Hey! Saw we both have " + sharedTrait + " — thought that was a good sign. "
                + "What's your move-in timeline looking like?";
    }
}