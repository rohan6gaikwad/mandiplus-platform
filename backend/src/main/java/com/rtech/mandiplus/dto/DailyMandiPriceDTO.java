package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyMandiPriceDTO {

    private Long id;
    private String state;
    private String district;
    private String market;
    private String commodity;
    private String variety;
    private String grade;
    private LocalDate arrivalDate;
    private String arrivalDateFormatted;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal modalPrice;
    private BigDecimal modalPricePerKg;
    private BigDecimal priceSpread;
    private String rawSource;
    private LocalDateTime importedAt;

    public DailyMandiPriceDTO() {
    }

    public DailyMandiPriceDTO(Long id, String state, String district, String market, String commodity,
                             String variety, String grade, LocalDate arrivalDate, String arrivalDateFormatted,
                             BigDecimal minPrice, BigDecimal maxPrice, BigDecimal modalPrice,
                             BigDecimal modalPricePerKg, BigDecimal priceSpread, String rawSource, LocalDateTime importedAt) {
        this.id = id;
        this.state = state;
        this.district = district;
        this.market = market;
        this.commodity = commodity;
        this.variety = variety;
        this.grade = grade;
        this.arrivalDate = arrivalDate;
        this.arrivalDateFormatted = arrivalDateFormatted;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.modalPrice = modalPrice;
        this.modalPricePerKg = modalPricePerKg;
        this.priceSpread = priceSpread;
        this.rawSource = rawSource;
        this.importedAt = importedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public String getCommodity() { return commodity; }
    public void setCommodity(String commodity) { this.commodity = commodity; }

    public String getVariety() { return variety; }
    public void setVariety(String variety) { this.variety = variety; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public LocalDate getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }

    public String getArrivalDateFormatted() { return arrivalDateFormatted; }
    public void setArrivalDateFormatted(String arrivalDateFormatted) { this.arrivalDateFormatted = arrivalDateFormatted; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public BigDecimal getModalPrice() { return modalPrice; }
    public void setModalPrice(BigDecimal modalPrice) { this.modalPrice = modalPrice; }

    public BigDecimal getModalPricePerKg() { return modalPricePerKg; }
    public void setModalPricePerKg(BigDecimal modalPricePerKg) { this.modalPricePerKg = modalPricePerKg; }

    public BigDecimal getPriceSpread() { return priceSpread; }
    public void setPriceSpread(BigDecimal priceSpread) { this.priceSpread = priceSpread; }

    public String getRawSource() { return rawSource; }
    public void setRawSource(String rawSource) { this.rawSource = rawSource; }

    public LocalDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(LocalDateTime importedAt) { this.importedAt = importedAt; }
}
