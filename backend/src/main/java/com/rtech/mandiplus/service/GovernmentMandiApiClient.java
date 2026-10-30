package com.rtech.mandiplus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * GovernmentMandiApiClient provides specialized integration with the official
 * Government of India Open Government Data (data.gov.in) portal for agricultural market prices.
 */
@Component
public class GovernmentMandiApiClient {

    private final GovMandiApiClient apiClient;

    @Autowired
    public GovernmentMandiApiClient(GovMandiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public boolean isApiKeyConfigured() {
        return apiClient.isApiKeyConfigured();
    }

    public String getResourceId() {
        return apiClient.getResourceId();
    }

    public GovMandiApiClient.ApiResponse fetchHistoricalRecords(String state, String district, String market, String commodity, String arrivalDate, int limit, int offset) {
        return apiClient.fetchMandiRecords(state, district, market, commodity, arrivalDate, limit, offset);
    }
}
