package com.roomsetu.ai;

import java.util.Optional;

/**
 * Deliberately provider-agnostic. Every service that uses this treats a failed
 * or disabled call as a normal, expected outcome (Optional.empty()) — not an
 * exception to catch. That's what lets every AI feature in Phase 6 fall back
 * to a deterministic template without special-casing errors at the call site.
 */
public interface AiTextClient {
    Optional<String> generate(String systemPrompt, String userPrompt);
}