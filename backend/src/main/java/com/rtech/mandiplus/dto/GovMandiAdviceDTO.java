package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GovMandiAdviceDTO {

    private String commodity;
    private String district;
    private String market;
    private String state;
    private String variety;
    private String grade;

    private LocalDate arrivalDate;
    private String reportingDateFormatted;

    // Actual Reported Prices (in ₹/quintal and ₹/kg)
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal modalPrice;
    private BigDecimal pricePerKg;

    // Mandi+ Suggested Selling-Price Range (transparently derived from historical moving average)
    private BigDecimal suggestedMinPrice;
    private BigDecimal suggestedMaxPrice;
    private BigDecimal suggestedMinPricePerKg;
    private BigDecimal suggestedMaxPricePerKg;
    private Boolean hasSuggestedRange;
    private String suggestionExplanation;

    // Trend & Baseline
    private String trend; // 'RISING', 'FALLING', 'STABLE', 'NOT_ENOUGH_DATA'
    private Double trendPercentage;
    private Integer historicalDataPoints;
    private BigDecimal movingAveragePrice;

    // Data Freshness & Warning
    private Boolean isStale;
    private String staleWarning;
    private String source;

    // Official Government Verified MSP
    private BigDecimal mspPrice;
    private Boolean isMspVerified;
    private String mspSeason;
    private String mspNotes;

    // Comparisons & Advisories
    private List<NearbyMarketPriceDTO> nearbyMarkets = new ArrayList<>();
    private List<GovMandiAdvisoryDTO> advisories = new ArrayList<>();

    public GovMandiAdviceDTO() {
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getReportingDateFormatted() {
        return reportingDateFormatted;
    }

    public void setReportingDateFormatted(String reportingDateFormatted) {
        this.reportingDateFormatted = reportingDateFormatted;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getModalPrice() {
        return modalPrice;
    }

    public void setModalPrice(BigDecimal modalPrice) {
        this.modalPrice = modalPrice;
    }

    public BigDecimal getPricePerKg() {
        return pricePerKg;
    }

    public void setPricePerKg(BigDecimal pricePerKg) {
        this.pricePerKg = pricePerKg;
    }

    public BigDecimal getSuggestedMinPrice() {
        return suggestedMinPrice;
    }

    public void setSuggestedMinPrice(BigDecimal suggestedMinPrice) {
        this.suggestedMinPrice = suggestedMinPrice;
    }

    public BigDecimal getSuggestedMaxPrice() {
        return suggestedMaxPrice;
    }

    public void setSuggestedMaxPrice(BigDecimal suggestedMaxPrice) {
        this.suggestedMaxPrice = suggestedMaxPrice;
    }

    public BigDecimal getSuggestedMinPricePerKg() {
        return suggestedMinPricePerKg;
    }

    public void setSuggestedMinPricePerKg(BigDecimal suggestedMinPricePerKg) {
        this.suggestedMinPricePerKg = suggestedMinPricePerKg;
    }

    public BigDecimal getSuggestedMaxPricePerKg() {
        return suggestedMaxPricePerKg;
    }

    public void setSuggestedMaxPricePerKg(BigDecimal suggestedMaxPricePerKg) {
        this.suggestedMaxPricePerKg = suggestedMaxPricePerKg;
    }

    public Boolean getHasSuggestedRange() {
        return hasSuggestedRange;
    }

    public void setHasSuggestedRange(Boolean hasSuggestedRange) {
        this.hasSuggestedRange = hasSuggestedRange;
    }

    public String getSuggestionExplanation() {
        return suggestionExplanation;
    }

    public void setSuggestionExplanation(String suggestionExplanation) {
        this.suggestionExplanation = suggestionExplanation;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public Double getTrendPercentage() {
        return trendPercentage;
    }

    public void setTrendPercentage(Double trendPercentage) {
        this.trendPercentage = trendPercentage;
    }

    public Integer getHistoricalDataPoints() {
        return historicalDataPoints;
    }

    public void setHistoricalDataPoints(Integer historicalDataPoints) {
        this.historicalDataPoints = historicalDataPoints;
    }

    public BigDecimal getMovingAveragePrice() {
        return movingAveragePrice;
    }

    public void setMovingAveragePrice(BigDecimal movingAveragePrice) {
        this.movingAveragePrice = movingAveragePrice;
    }

    public Boolean getIsStale() {
        return isStale;
    }

    public void setIsStale(Boolean isStale) {
        this.isStale = isStale;
    }

    public String getStaleWarning() {
        return staleWarning;
    }

    public void setStaleWarning(String staleWarning) {
        this.staleWarning = staleWarning;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BigDecimal getMspPrice() {
        return mspPrice;
    }

    public void setMspPrice(BigDecimal mspPrice) {
        this.mspPrice = mspPrice;
    }

    public Boolean getIsMspVerified() {
        return isMspVerified;
    }

    public void setIsMspVerified(Boolean isMspVerified) {
        this.isMspVerified = isMspVerified;
    }

    public String getMspSeason() {
        return mspSeason;
    }

    public void setMspSeason(String mspSeason) {
        this.mspSeason = mspSeason;
    }

    public String getMspNotes() {
        return mspNotes;
    }

    public void setMspNotes(String mspNotes) {
        this.mspNotes = mspNotes;
    }

    public List<NearbyMarketPriceDTO> getNearbyMarkets() {
        return nearbyMarkets;
    }

    public void setNearbyMarkets(List<NearbyMarketPriceDTO> nearbyMarkets) {
        this.nearbyMarkets = nearbyMarkets;
    }

    public List<GovMandiAdvisoryDTO> getAdvisories() {
        return advisories;
    }

    public void setAdvisories(List<GovMandiAdvisoryDTO> advisories) {
        this.advisories = advisories;
    }
}
