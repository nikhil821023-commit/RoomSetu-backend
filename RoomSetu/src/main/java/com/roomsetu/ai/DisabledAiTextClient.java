
package com.roomsetu.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(
        prefix = "roomsetu.ai",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class DisabledAiTextClient implements AiTextClient {

    @Override
    public Optional<String> generate(String systemPrompt, String userPrompt) {
        return Optional.empty();
    }
}
