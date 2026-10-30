package com.rtech.mandiplus.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GovMandiHistoryResponseDTO {

    private String commodity;
    private String state;
    private String district;
    private String market;
    private Integer year;
    private Integer month;
    private String viewMode; // "YEAR", "MONTH", "DAY", "CUSTOM"
    private LocalDate startDate;
    private LocalDate endDate;

    private GovMandiHistorySummaryDTO summary;
    private List<MonthlyPriceSummaryDTO> monthlyAverages = new ArrayList<>();
    private List<YearlyPriceSummaryDTO> yearlyAverages = new ArrayList<>();
    private List<DailyMandiPriceDTO> dailyPrices = new ArrayList<>();
    private List<Integer> availableYears = new ArrayList<>();

    private String dataSource;
    private LocalDateTime lastSyncTimestamp;
    private boolean apiKeyConfigured;
    private String resourceId;
    private boolean dataAvailable;
    private String statusMessage;

    public GovMandiHistoryResponseDTO() {
    }

    public String getCommodity() { return commodity; }
    public void setCommodity(String commodity) { this.commodity = commodity; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public String getViewMode() { return viewMode; }
    public void setViewMode(String viewMode) { this.viewMode = viewMode; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public GovMandiHistorySummaryDTO getSummary() { return summary; }
    public void setSummary(GovMandiHistorySummaryDTO summary) { this.summary = summary; }

    public List<MonthlyPriceSummaryDTO> getMonthlyAverages() { return monthlyAverages; }
    public void setMonthlyAverages(List<MonthlyPriceSummaryDTO> monthlyAverages) { this.monthlyAverages = monthlyAverages; }

    public List<YearlyPriceSummaryDTO> getYearlyAverages() { return yearlyAverages; }
    public void setYearlyAverages(List<YearlyPriceSummaryDTO> yearlyAverages) { this.yearlyAverages = yearlyAverages; }

    public List<DailyMandiPriceDTO> getDailyPrices() { return dailyPrices; }
    public void setDailyPrices(List<DailyMandiPriceDTO> dailyPrices) { this.dailyPrices = dailyPrices; }

    public List<Integer> getAvailableYears() { return availableYears; }
    public void setAvailableYears(List<Integer> availableYears) { this.availableYears = availableYears; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public LocalDateTime getLastSyncTimestamp() { return lastSyncTimestamp; }
    public void setLastSyncTimestamp(LocalDateTime lastSyncTimestamp) { this.lastSyncTimestamp = lastSyncTimestamp; }

    public boolean isApiKeyConfigured() { return apiKeyConfigured; }
    public void setApiKeyConfigured(boolean apiKeyConfigured) { this.apiKeyConfigured = apiKeyConfigured; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public boolean isDataAvailable() { return dataAvailable; }
    public void setDataAvailable(boolean dataAvailable) { this.dataAvailable = dataAvailable; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
