package com.roomsetu.config;

// Intentionally empty for now — AnthropicAiTextClient is a @Component and self-configures
// via @Value. This class exists as the obvious place to add a proper RestClient/WebClient
// bean, retry policy, or a second provider (for an A/B or fallback-provider setup) later,
// without every service needing to know it changed.
public class AiConfig {
}