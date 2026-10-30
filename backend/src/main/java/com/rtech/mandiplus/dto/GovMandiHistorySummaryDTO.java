package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GovMandiHistorySummaryDTO {

    private BigDecimal averageModalPrice;
    private BigDecimal averageModalPricePerKg;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal highestPrice;
    private LocalDate highestPriceDate;
    private String highestPriceMarket;
    private BigDecimal lowestPrice;
    private LocalDate lowestPriceDate;
    private String lowestPriceMarket;
    private int totalArrivalRecords;
    private int totalTradingDays;
    private String trend; // "RISING", "FALLING", "STABLE", "INSUFFICIENT_DATA"
    private Double priceChangePercentage;
    private String comparisonText;

    public GovMandiHistorySummaryDTO() {
    }

    public BigDecimal getAverageModalPrice() { return averageModalPrice; }
    public void setAverageModalPrice(BigDecimal averageModalPrice) { this.averageModalPrice = averageModalPrice; }

    public BigDecimal getAverageModalPricePerKg() { return averageModalPricePerKg; }
    public void setAverageModalPricePerKg(BigDecimal averageModalPricePerKg) { this.averageModalPricePerKg = averageModalPricePerKg; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public BigDecimal getHighestPrice() { return highestPrice; }
    public void setHighestPrice(BigDecimal highestPrice) { this.highestPrice = highestPrice; }

    public LocalDate getHighestPriceDate() { return highestPriceDate; }
    public void setHighestPriceDate(LocalDate highestPriceDate) { this.highestPriceDate = highestPriceDate; }

    public String getHighestPriceMarket() { return highestPriceMarket; }
    public void setHighestPriceMarket(String highestPriceMarket) { this.highestPriceMarket = highestPriceMarket; }

    public BigDecimal getLowestPrice() { return lowestPrice; }
    public void setLowestPrice(BigDecimal lowestPrice) { this.lowestPrice = lowestPrice; }

    public LocalDate getLowestPriceDate() { return lowestPriceDate; }
    public void setLowestPriceDate(LocalDate lowestPriceDate) { this.lowestPriceDate = lowestPriceDate; }

    public String getLowestPriceMarket() { return lowestPriceMarket; }
    public void setLowestPriceMarket(String lowestPriceMarket) { this.lowestPriceMarket = lowestPriceMarket; }

    public int getTotalArrivalRecords() { return totalArrivalRecords; }
    public void setTotalArrivalRecords(int totalArrivalRecords) { this.totalArrivalRecords = totalArrivalRecords; }

    public int getTotalTradingDays() { return totalTradingDays; }
    public void setTotalTradingDays(int totalTradingDays) { this.totalTradingDays = totalTradingDays; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public Double getPriceChangePercentage() { return priceChangePercentage; }
    public void setPriceChangePercentage(Double priceChangePercentage) { this.priceChangePercentage = priceChangePercentage; }

    public String getComparisonText() { return comparisonText; }
    public void setComparisonText(String comparisonText) { this.comparisonText = comparisonText; }
}
