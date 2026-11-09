package com.roomsetu.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Sends over HTTPS, so it works where SMTP ports are blocked (Render's free plan). */
@Component
@ConditionalOnExpression("'${roomsetu.mail.provider:smtp}'.trim().equalsIgnoreCase('brevo')")
public class BrevoEmailSender implements EmailSender {

    @Value("${roomsetu.mail.enabled:false}")
    private boolean enabled;

    @Value("${roomsetu.mail.from:}")
    private String from;

    @Value("${roomsetu.mail.from-name:RoomSetu}")
    private String fromName;

    @Value("${roomsetu.mail.brevo.api-key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean send(String to, String subject, String body) {
        if (!enabled) {
            System.err.println("[Mail] Not sent: roomsetu.mail.enabled is false");
            return false;
        }
        if (apiKey == null || apiKey.isBlank() || from == null || from.isBlank()) {
            System.err.println("[Mail] Not sent: BREVO_API_KEY or MAIL_FROM is empty");
            return false;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", fromName, "email", from.trim()),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", body
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(10))
                    .header("api-key", apiKey.trim())
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean ok = response.statusCode() / 100 == 2;
            if (!ok) {
                System.err.printf("[Mail] Brevo rejected the request (%d): %s%n", response.statusCode(), response.body());
            }
            return ok;

        } catch (Exception e) {
            System.err.println("[Mail] Brevo call failed (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            return false;
        }
    }
}