package com.rtech.mandiplus.dto;

import java.math.BigDecimal;

public class MonthlyPriceSummaryDTO {

    private int year;
    private int month;
    private String monthName;
    private BigDecimal averageModalPrice;
    private BigDecimal averageModalPricePerKg;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal highestModalPrice;
    private BigDecimal lowestModalPrice;
    private int recordsCount;
    private int tradingDaysCount;
    private Double monthOverMonthChangePercentage;
    private String trendDirection; // "RISING", "FALLING", "STABLE"

    public MonthlyPriceSummaryDTO() {
    }

    public MonthlyPriceSummaryDTO(int year, int month, String monthName, BigDecimal averageModalPrice,
                                 BigDecimal averageModalPricePerKg, BigDecimal minPrice, BigDecimal maxPrice,
                                 BigDecimal highestModalPrice, BigDecimal lowestModalPrice,
                                 int recordsCount, int tradingDaysCount, Double monthOverMonthChangePercentage,
                                 String trendDirection) {
        this.year = year;
        this.month = month;
        this.monthName = monthName;
        this.averageModalPrice = averageModalPrice;
        this.averageModalPricePerKg = averageModalPricePerKg;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.highestModalPrice = highestModalPrice;
        this.lowestModalPrice = lowestModalPrice;
        this.recordsCount = recordsCount;
        this.tradingDaysCount = tradingDaysCount;
        this.monthOverMonthChangePercentage = monthOverMonthChangePercentage;
        this.trendDirection = trendDirection;
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

    public BigDecimal getAverageModalPrice() { return averageModalPrice; }
    public void setAverageModalPrice(BigDecimal averageModalPrice) { this.averageModalPrice = averageModalPrice; }

    public BigDecimal getAverageModalPricePerKg() { return averageModalPricePerKg; }
    public void setAverageModalPricePerKg(BigDecimal averageModalPricePerKg) { this.averageModalPricePerKg = averageModalPricePerKg; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public BigDecimal getHighestModalPrice() { return highestModalPrice; }
    public void setHighestModalPrice(BigDecimal highestModalPrice) { this.highestModalPrice = highestModalPrice; }

    public BigDecimal getLowestModalPrice() { return lowestModalPrice; }
    public void setLowestModalPrice(BigDecimal lowestModalPrice) { this.lowestModalPrice = lowestModalPrice; }

    public int getRecordsCount() { return recordsCount; }
    public void setRecordsCount(int recordsCount) { this.recordsCount = recordsCount; }

    public int getTradingDaysCount() { return tradingDaysCount; }
    public void setTradingDaysCount(int tradingDaysCount) { this.tradingDaysCount = tradingDaysCount; }

    public Double getMonthOverMonthChangePercentage() { return monthOverMonthChangePercentage; }
    public void setMonthOverMonthChangePercentage(Double monthOverMonthChangePercentage) { this.monthOverMonthChangePercentage = monthOverMonthChangePercentage; }

    public String getTrendDirection() { return trendDirection; }
    public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }
}
