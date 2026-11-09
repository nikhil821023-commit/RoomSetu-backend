package com.roomsetu.sms;

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

/**
 * Fast2SMS's OTP route sends through their own pre-approved template, so RoomSetu
 * doesn't need its own DLT registration. The message arrives as "Your OTP: 123456".
 * Same fail-soft pattern as the other gateways: disabled, misconfigured or rejected
 * returns false and OtpService decides what that means.
 */
@Component
@ConditionalOnProperty(name = "roomsetu.sms.provider", havingValue = "fast2sms")
public class Fast2SmsGateway implements SmsGateway {

    @Value("${roomsetu.sms.enabled:false}")
    private boolean enabled;

    @Value("${roomsetu.sms.fast2sms.api-key:}")
    private String apiKey;

    @Value("${roomsetu.sms.fast2sms.base-url:https://www.fast2sms.com/dev/bulkV2}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean sendOtp(String phoneNumber, String otp) {
        if (!enabled || apiKey == null || apiKey.isBlank()) return false;

        try {
            // POST keeps the key and the code out of the URL, so they never land in access logs.
            Map<String, Object> body = Map.of(
                    "route", "otp",
                    "variables_values", otp,
                    "numbers", phoneNumber   // 10-digit Indian number, no country code
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(8))
                    .header("authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());
            boolean accepted = response.statusCode() == 200 && root.path("return").asBoolean(false);

            if (!accepted) {
                // Prints their actual reply (wrong key, empty wallet, and so on) so a failure is diagnosable.
                System.err.printf("[SMS] Fast2SMS rejected the request (%d): %s%n",
                        response.statusCode(), response.body());
            }
            return accepted;

        } catch (Exception e) {
            System.err.println("[SMS] Fast2SMS call failed: " + e.getMessage());
            return false;
        }
    }
}