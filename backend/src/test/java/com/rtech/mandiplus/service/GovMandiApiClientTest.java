package com.rtech.mandiplus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class GovMandiApiClientTest {

    private GovMandiApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new GovMandiApiClient();
        ReflectionTestUtils.setField(apiClient, "baseUrl", "https://api.data.gov.in/resource");
        ReflectionTestUtils.setField(apiClient, "resourceId", "9ef84268-d588-465a-a308-a864a43d0070");
    }

    @Test
    @DisplayName("Test that missing API key throws IllegalStateException and does not fake data")
    void testMissingApiKeyThrowsException() {
        ReflectionTestUtils.setField(apiClient, "apiKey", "");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            apiClient.fetchMandiRecords("Maharashtra", null, null, 10, 0);
        });

        assertTrue(ex.getMessage().contains("DATA_GOV_IN_API_KEY"));
    }

    @Test
    @DisplayName("Test API key is scrubbed from URLs and error logs")
    void testApiKeySanitizationInUrls() {
        String testKey = "dummy_sample_api_key_12345";
        ReflectionTestUtils.setField(apiClient, "apiKey", testKey);

        String sampleUrl = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070?api-key=" + testKey + "&format=json&limit=10";
        String sanitized = apiClient.sanitizeUrl(sampleUrl);

        assertFalse(sanitized.contains(testKey), "Sanitized URL must NEVER contain the secret API key!");
        assertTrue(sanitized.contains("api-key=[REDACTED]"), "Sanitized URL must replace secret key with [REDACTED]");
    }

    @Test
    @DisplayName("Test that error messages and exceptions never leak the raw API key")
    void testExceptionNeverExposesKey() {
        String testKey = "super_secret_api_key_123456789";
        ReflectionTestUtils.setField(apiClient, "apiKey", testKey);
        // Point to an invalid host so it fails
        ReflectionTestUtils.setField(apiClient, "baseUrl", "http://invalid-non-existent-host-9999.gov.in");

        Exception ex = assertThrows(Exception.class, () -> {
            apiClient.fetchMandiRecords("Maharashtra", null, null, 10, 0);
        });

        assertFalse(ex.getMessage().contains(testKey), "Exception message must never contain the secret API key!");
    }
}
