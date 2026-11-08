package com.roomsetu.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "roomsetu.sms.provider", havingValue = "msg91", matchIfMissing = true)
public class Msg91SmsGateway implements SmsGateway {

    @Value("${roomsetu.sms.enabled}")
    private boolean enabled;

    @Value("${roomsetu.sms.msg91.auth-key}")
    private String authKey;

    @Value("${roomsetu.sms.msg91.template-id}")
    private String templateId;

    @Value("${roomsetu.sms.msg91.base-url}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean sendOtp(String phoneNumber, String otp) {
        if (!enabled || authKey == null || authKey.isBlank()) {
            // Same intentional pattern as the AI client: disabled/misconfigured
            // is not an error, it's a mode. OtpService logs the OTP to console instead.
            return false;
        }

        try {
            // MSG91's OTP endpoint expects the number with country code, no plus sign.
            String fullNumber = "91" + phoneNumber;

            String url = baseUrl + "/otp"
                    + "?template_id=" + templateId
                    + "&mobile=" + fullNumber
                    + "&authkey=" + authKey
                    + "&otp=" + otp;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.printf("[SMS] MSG91 returned %d: %s%n", response.statusCode(), response.body());
                return false;
            }
            return true;

        } catch (Exception e) {
            System.err.println("[SMS] Delivery failed: " + e.getMessage());
            return false;
        }
    }
}