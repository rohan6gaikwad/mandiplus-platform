package com.rtech.mandiplus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GovMandiApiClient {

    private static final Logger logger = LoggerFactory.getLogger(GovMandiApiClient.class);

    @Value("${gov.mandi.api.key:${DATA_GOV_IN_API_KEY:}}")
    private String apiKey;

    @Value("${gov.mandi.api.resource-id:9ef84268-d588-465a-a308-a864a43d0070}")
    private String resourceId;

    @Value("${gov.mandi.api.base-url:https://api.data.gov.in/resource}")
    private String baseUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GovMandiApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equalsIgnoreCase("YOUR_API_KEY");
    }

    public String getResourceId() {
        return resourceId;
    }

    /**
     * Fetches official Mandi records from data.gov.in API with filters and pagination.
     */
    public ApiResponse fetchMandiRecords(String state, String district, String commodity, int limit, int offset) {
        if (!isApiKeyConfigured()) {
            throw new IllegalStateException("Government API key is not configured. Please set the DATA_GOV_IN_API_KEY environment variable to enable data synchronization.");
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl.replaceAll("/+$", "")).append("/").append(resourceId)
                .append("?api-key=").append(URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8))
                .append("&format=json")
                .append("&limit=").append(limit)
                .append("&offset=").append(offset);

        if (state != null && !state.trim().isEmpty()) {
            urlBuilder.append("&filters%5Bstate%5D=").append(URLEncoder.encode(state.trim(), StandardCharsets.UTF_8));
        }
        if (district != null && !district.trim().isEmpty()) {
            urlBuilder.append("&filters%5Bdistrict%5D=").append(URLEncoder.encode(district.trim(), StandardCharsets.UTF_8));
        }
        if (commodity != null && !commodity.trim().isEmpty()) {
            urlBuilder.append("&filters%5Bcommodity%5D=").append(URLEncoder.encode(commodity.trim(), StandardCharsets.UTF_8));
        }

        String targetUrl = urlBuilder.toString();
        // Safe URL for logging without revealing secret key
        String safeUrl = targetUrl.replaceAll("api-key=[^&]+", "api-key=[REDACTED]");
        logger.info("Calling official data.gov.in API: {}", safeUrl);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401 || response.statusCode() == 403) {
                logger.error("data.gov.in authentication rejected: HTTP {}", response.statusCode());
                throw new RuntimeException("Upstream data.gov.in API rejected the API key (HTTP " + response.statusCode() + "). Please check that your DATA_GOV_IN_API_KEY is active and valid on data.gov.in.");
            }

            if (response.statusCode() != 200) {
                logger.error("data.gov.in returned error: HTTP {}", response.statusCode());
                throw new RuntimeException("Upstream data.gov.in API error (HTTP " + response.statusCode() + "): " + sanitizeError(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            String status = root.path("status").asText("ok");
            int total = root.path("total").asInt(0);
            int count = root.path("count").asInt(0);
            String updatedDate = root.path("updated_date").asText("");

            List<Map<String, Object>> recordsList = new ArrayList<>();
            JsonNode recordsNode = root.path("records");
            if (recordsNode.isArray()) {
                for (JsonNode item : recordsNode) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("state", item.path("state").asText(""));
                    map.put("district", item.path("district").asText(""));
                    map.put("market", item.path("market").asText(""));
                    map.put("commodity", item.path("commodity").asText(""));
                    map.put("variety", item.path("variety").asText("Other"));
                    map.put("grade", item.path("grade").asText("FAQ"));
                    map.put("arrival_date", item.path("arrival_date").asText(""));
                    map.put("min_price", item.path("min_price").asDouble(0.0));
                    map.put("max_price", item.path("max_price").asDouble(0.0));
                    map.put("modal_price", item.path("modal_price").asDouble(0.0));
                    recordsList.add(map);
                }
            }

            return new ApiResponse(status, total, count, updatedDate, recordsList);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("data.gov.in API request interrupted", e);
        } catch (Exception e) {
            String sanitizedMsg = sanitizeError(e.getMessage());
            logger.error("Failed to fetch official Mandi data: {}", sanitizedMsg);
            throw new RuntimeException(sanitizedMsg, e);
        }
    }

    public String sanitizeUrl(String raw) {
        return sanitizeError(raw);
    }

    public String sanitizeError(String raw) {
        if (raw == null) return "Unknown error";
        if (apiKey != null && !apiKey.isEmpty()) {
            return raw.replace(apiKey, "[REDACTED]").replaceAll("api-key=[^&\\s]+", "api-key=[REDACTED]");
        }
        return raw.replaceAll("api-key=[^&\\s]+", "api-key=[REDACTED]");
    }

    public static class ApiResponse {
        private final String status;
        private final int total;
        private final int count;
        private final String updatedDate;
        private final List<Map<String, Object>> records;

        public ApiResponse(String status, int total, int count, String updatedDate, List<Map<String, Object>> records) {
            this.status = status;
            this.total = total;
            this.count = count;
            this.updatedDate = updatedDate;
            this.records = records;
        }

        public String getStatus() { return status; }
        public int getTotal() { return total; }
        public int getCount() { return count; }
        public String getUpdatedDate() { return updatedDate; }
        public List<Map<String, Object>> getRecords() { return records; }
    }
}
