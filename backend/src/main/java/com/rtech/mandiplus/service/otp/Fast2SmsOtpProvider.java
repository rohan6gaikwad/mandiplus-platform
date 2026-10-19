package com.rtech.mandiplus.service.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Real SMS OTP Provider for Indian Mobile Numbers via Fast2SMS Gateway.
 * Supports DLT-compliant Quick OTP route for India numbers (+91).
 */
@Service
@ConditionalOnProperty(name = "mandiplus.otp.provider", havingValue = "fast2sms", matchIfMissing = true)
public class Fast2SmsOtpProvider implements OtpProvider {

    private static final Logger logger = LoggerFactory.getLogger(Fast2SmsOtpProvider.class);

    @Value("${mandiplus.otp.api-key:${MANDIPLUS_OTP_API_KEY:}}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public OtpSendResult sendOtp(String phoneNumber, String otp, int expiryMinutes) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            String errorMsg = "SMS Gateway API key is missing. Real SMS cannot be delivered to SIM card +91-" 
                    + phoneNumber + ". Please configure MANDIPLUS_OTP_API_KEY with your Fast2SMS API key in .env or application.properties.";
            logger.error("[OTP-Provider] {}", errorMsg);
            return OtpSendResult.failure(errorMsg);
        }

        try {
            // Attempt 1: Fast2SMS Quick OTP route
            String requestUrl = String.format(
                    "https://www.fast2sms.com/dev/bulkV2?authorization=%s&route=otp&variables_values=%s&flash=0&numbers=%s",
                    URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8),
                    URLEncoder.encode(otp, StandardCharsets.UTF_8),
                    URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("authorization", apiKey.trim())
                    .header("cache-control", "no-cache")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body().contains("\"return\":true")) {
                logger.info("[OTP-Provider] Real SMS OTP dispatched successfully to +91-{} via Fast2SMS (route=otp).", phoneNumber);
                return OtpSendResult.success("F2S-OTP-" + System.currentTimeMillis());
            }

            // Attempt 2: Fallback to Quick SMS route (route=q)
            logger.warn("[OTP-Provider] route=otp response: {}. Attempting fallback to route=q...", response.body());
            String quickMsg = "Your MandiPlus verification OTP is " + otp + ". Do not share this code with anyone.";
            String quickUrl = String.format(
                    "https://www.fast2sms.com/dev/bulkV2?authorization=%s&route=q&message=%s&language=english&flash=0&numbers=%s",
                    URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8),
                    URLEncoder.encode(quickMsg, StandardCharsets.UTF_8),
                    URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8)
            );

            HttpRequest quickRequest = HttpRequest.newBuilder()
                    .uri(URI.create(quickUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("authorization", apiKey.trim())
                    .header("cache-control", "no-cache")
                    .GET()
                    .build();

            HttpResponse<String> quickResponse = httpClient.send(quickRequest, HttpResponse.BodyHandlers.ofString());

            if (quickResponse.statusCode() == 200 && quickResponse.body().contains("\"return\":true")) {
                logger.info("[OTP-Provider] Real SMS OTP dispatched successfully to +91-{} via Fast2SMS (route=q).", phoneNumber);
                return OtpSendResult.success("F2S-Q-" + System.currentTimeMillis());
            } else {
                logger.error("[OTP-Provider] Fast2SMS returned error: {}", quickResponse.body());
                return OtpSendResult.failure("Fast2SMS delivery failed: " + quickResponse.body());
            }
        } catch (Exception e) {
            logger.error("[OTP-Provider] Error connecting to SMS provider gateway: {}", e.getMessage());
            return OtpSendResult.failure("Failed to connect to SMS delivery gateway: " + e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "FAST2SMS";
    }
}
