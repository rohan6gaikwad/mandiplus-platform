package com.rtech.mandiplus.dto;

import com.rtech.mandiplus.entity.GovMandiSyncLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GovMandiSyncStatusDTO {

    private Boolean isApiKeyConfigured;
    private String resourceId;
    private LocalDateTime lastSyncTimestamp;
    private String lastSyncStatus;
    private Long totalRecords;
    private Long totalCommodities;
    private Long totalMarkets;
    private Boolean isDataFresh;
    private LocalDate latestArrivalDate;
    private String message;
    private List<GovMandiSyncLog> recentLogs;

    public GovMandiSyncStatusDTO() {
    }

    public Boolean getIsApiKeyConfigured() {
        return isApiKeyConfigured;
    }

    public void setIsApiKeyConfigured(Boolean apiKeyConfigured) {
        isApiKeyConfigured = apiKeyConfigured;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDateTime getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }

    public void setLastSyncTimestamp(LocalDateTime lastSyncTimestamp) {
        this.lastSyncTimestamp = lastSyncTimestamp;
    }

    public String getLastSyncStatus() {
        return lastSyncStatus;
    }

    public void setLastSyncStatus(String lastSyncStatus) {
        this.lastSyncStatus = lastSyncStatus;
    }

    public Long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Long getTotalCommodities() {
        return totalCommodities;
    }

    public void setTotalCommodities(Long totalCommodities) {
        this.totalCommodities = totalCommodities;
    }

    public Long getTotalMarkets() {
        return totalMarkets;
    }

    public void setTotalMarkets(Long totalMarkets) {
        this.totalMarkets = totalMarkets;
    }

    public Boolean getIsDataFresh() {
        return isDataFresh;
    }

    public void setIsDataFresh(Boolean dataFresh) {
        isDataFresh = dataFresh;
    }

    public LocalDate getLatestArrivalDate() {
        return latestArrivalDate;
    }

    public void setLatestArrivalDate(LocalDate latestArrivalDate) {
        this.latestArrivalDate = latestArrivalDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GovMandiSyncLog> getRecentLogs() {
        return recentLogs;
    }

    public void setRecentLogs(List<GovMandiSyncLog> recentLogs) {
        this.recentLogs = recentLogs;
    }
}
