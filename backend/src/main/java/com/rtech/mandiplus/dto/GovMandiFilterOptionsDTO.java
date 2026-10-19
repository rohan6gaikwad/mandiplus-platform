package com.rtech.mandiplus.dto;

import java.util.List;
import java.util.Map;

public class GovMandiFilterOptionsDTO {

    private String state;
    private List<String> districts;
    private List<String> commodities;
    private Map<String, List<String>> districtMarkets;

    public GovMandiFilterOptionsDTO() {
    }

    public GovMandiFilterOptionsDTO(String state, List<String> districts, List<String> commodities, Map<String, List<String>> districtMarkets) {
        this.state = state;
        this.districts = districts;
        this.commodities = commodities;
        this.districtMarkets = districtMarkets;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getDistricts() {
        return districts;
    }

    public void setDistricts(List<String> districts) {
        this.districts = districts;
    }

    public List<String> getCommodities() {
        return commodities;
    }

    public void setCommodities(List<String> commodities) {
        this.commodities = commodities;
    }

    public Map<String, List<String>> getDistrictMarkets() {
        return districtMarkets;
    }

    public void setDistrictMarkets(Map<String, List<String>> districtMarkets) {
        this.districtMarkets = districtMarkets;
    }
}
