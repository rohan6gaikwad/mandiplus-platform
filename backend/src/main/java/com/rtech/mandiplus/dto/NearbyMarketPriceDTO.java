package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NearbyMarketPriceDTO {

    private String market;
    private String district;
    private BigDecimal modalPrice;
    private BigDecimal pricePerKg;
    private BigDecimal priceDifference; // compared to selected market
    private LocalDate arrivalDate;
    private String distanceNote;

    public NearbyMarketPriceDTO() {
    }

    public NearbyMarketPriceDTO(String market, String district, BigDecimal modalPrice,
                                BigDecimal pricePerKg, BigDecimal priceDifference,
                                LocalDate arrivalDate, String distanceNote) {
        this.market = market;
        this.district = district;
        this.modalPrice = modalPrice;
        this.pricePerKg = pricePerKg;
        this.priceDifference = priceDifference;
        this.arrivalDate = arrivalDate;
        this.distanceNote = distanceNote;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
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

    public BigDecimal getPriceDifference() {
        return priceDifference;
    }

    public void setPriceDifference(BigDecimal priceDifference) {
        this.priceDifference = priceDifference;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getDistanceNote() {
        return distanceNote;
    }

    public void setDistanceNote(String distanceNote) {
        this.distanceNote = distanceNote;
    }
}
