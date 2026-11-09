package com.roomsetu.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Uses java.net.http.HttpClient rather than adding WebClient/RestTemplate as a new
 * dependency — this is the only external HTTP call in the whole backend so far,
 * and it doesn't justify pulling in spring-webflux just for one integration.
 */
@Component
@ConditionalOnProperty(
        prefix = "roomsetu.ai",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class AnthropicAiTextClient implements AiTextClient {

    @Value("${roomsetu.ai.enabled:false}")
    private boolean enabled;

    @Value("${roomsetu.ai.api-key:}")
    private String apiKey;

    @Value("${roomsetu.ai.model:claude-sonnet-4-5}")
    private String model;

    @Value("${roomsetu.ai.timeout-ms:10000}")
    private long timeoutMs;

    @Value("${roomsetu.ai.max-tokens:500}")
    private int maxTokens;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Optional<String> generate(String systemPrompt, String userPrompt) {
        if (!enabled || apiKey == null || apiKey.isBlank()) {
            return Optional.empty(); // not an error — caller falls back to its template
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "max_tokens", maxTokens,
                    "system", systemPrompt,
                    "messages", new Object[]{ Map.of("role", "user", "content", userPrompt) }
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.anthropic.com/v1/messages"))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.printf("[AI] Anthropic API returned %d: %s%n", response.statusCode(), response.body());
                return Optional.empty();
            }

            JsonNode root = mapper.readTree(response.body());
            String text = root.path("content").path(0).path("text").asText(null);
            return Optional.ofNullable(text).map(String::trim);

        } catch (Exception e) {
            // Network hiccup, timeout, malformed response — all treated the same:
            // log it, return empty, let the caller's template fallback handle it.
            System.err.println("[AI] Generation failed, falling back to template: " + e.getMessage());
            return Optional.empty();
        }
    }
}