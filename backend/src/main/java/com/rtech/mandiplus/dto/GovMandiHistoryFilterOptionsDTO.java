package com.rtech.mandiplus.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class GovMandiHistoryFilterOptionsDTO {

    private String state;
    private List<String> states;
    private List<String> districts;
    private List<String> commodities;
    private Map<String, List<String>> districtMarkets;
    private List<Integer> availableYears;
    private LocalDate minDate;
    private LocalDate maxDate;
    private LocalDateTime lastSyncTime;
    private Long totalRecords;

    public GovMandiHistoryFilterOptionsDTO() {
    }

    public GovMandiHistoryFilterOptionsDTO(String state, List<String> states, List<String> districts,
                                          List<String> commodities, Map<String, List<String>> districtMarkets,
                                          List<Integer> availableYears, LocalDate minDate, LocalDate maxDate,
                                          LocalDateTime lastSyncTime, Long totalRecords) {
        this.state = state;
        this.states = states;
        this.districts = districts;
        this.commodities = commodities;
        this.districtMarkets = districtMarkets;
        this.availableYears = availableYears;
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.lastSyncTime = lastSyncTime;
        this.totalRecords = totalRecords;
    }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public List<String> getStates() { return states; }
    public void setStates(List<String> states) { this.states = states; }

    public List<String> getDistricts() { return districts; }
    public void setDistricts(List<String> districts) { this.districts = districts; }

    public List<String> getCommodities() { return commodities; }
    public void setCommodities(List<String> commodities) { this.commodities = commodities; }

    public Map<String, List<String>> getDistrictMarkets() { return districtMarkets; }
    public void setDistrictMarkets(Map<String, List<String>> districtMarkets) { this.districtMarkets = districtMarkets; }

    public List<Integer> getAvailableYears() { return availableYears; }
    public void setAvailableYears(List<Integer> availableYears) { this.availableYears = availableYears; }

    public LocalDate getMinDate() { return minDate; }
    public void setMinDate(LocalDate minDate) { this.minDate = minDate; }

    public LocalDate getMaxDate() { return maxDate; }
    public void setMaxDate(LocalDate maxDate) { this.maxDate = maxDate; }

    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }

    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }
}
