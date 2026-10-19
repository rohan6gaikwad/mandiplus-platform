package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mandi_prices")
public class MandiPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(length = 100)
    private String variety;

    @Column(name = "market_name", nullable = false, length = 100)
    private String marketName;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(name = "price_per_quintal", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerQuintal;

    @Column(name = "price_change_yesterday", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceChangeYesterday;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public MandiPrice() {
    }

    public MandiPrice(Long id, String cropName, String variety, String marketName, String state,
                      BigDecimal pricePerQuintal, BigDecimal priceChangeYesterday, LocalDateTime updatedAt) {
        this.id = id;
        this.cropName = cropName;
        this.variety = variety;
        this.marketName = marketName;
        this.state = state;
        this.pricePerQuintal = pricePerQuintal;
        this.priceChangeYesterday = priceChangeYesterday;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getPricePerQuintal() {
        return pricePerQuintal;
    }

    public void setPricePerQuintal(BigDecimal pricePerQuintal) {
        this.pricePerQuintal = pricePerQuintal;
    }

    public BigDecimal getPriceChangeYesterday() {
        return priceChangeYesterday;
    }

    public void setPriceChangeYesterday(BigDecimal priceChangeYesterday) {
        this.priceChangeYesterday = priceChangeYesterday;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void fluctuate(double change) {
        BigDecimal oldPrice = this.pricePerQuintal;
        BigDecimal diff = BigDecimal.valueOf(change).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal newPrice = oldPrice.add(diff).setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal minPrice = BigDecimal.valueOf(10.0);
        BigDecimal maxPrice = BigDecimal.valueOf(999999.99);

        if (this.id != null) {
            if (this.id == 1) { // Onion
                minPrice = BigDecimal.valueOf(1400.0);
                maxPrice = BigDecimal.valueOf(2500.0);
            } else if (this.id == 2) { // Grapes
                minPrice = BigDecimal.valueOf(6000.0);
                maxPrice = BigDecimal.valueOf(9500.0);
            } else if (this.id == 3) { // Sugarcane
                minPrice = BigDecimal.valueOf(250.0);
                maxPrice = BigDecimal.valueOf(450.0);
            } else if (this.id == 4) { // Soybean
                minPrice = BigDecimal.valueOf(3800.0);
                maxPrice = BigDecimal.valueOf(5500.0);
            } else if (this.id == 5) { // Alphonso Mango
                minPrice = BigDecimal.valueOf(9000.0);
                maxPrice = BigDecimal.valueOf(17000.0);
            }
        }

        if (newPrice.compareTo(minPrice) < 0) {
            newPrice = minPrice;
            diff = newPrice.subtract(oldPrice);
        } else if (newPrice.compareTo(maxPrice) > 0) {
            newPrice = maxPrice;
            diff = newPrice.subtract(oldPrice);
        }

        this.pricePerQuintal = newPrice;

        BigDecimal newChange = this.priceChangeYesterday.add(diff).setScale(2, java.math.RoundingMode.HALF_UP);
        if (newChange.compareTo(BigDecimal.valueOf(999.99)) > 0) {
            newChange = BigDecimal.valueOf(999.99);
        } else if (newChange.compareTo(BigDecimal.valueOf(-999.99)) < 0) {
            newChange = BigDecimal.valueOf(-999.99);
        }
        this.priceChangeYesterday = newChange;
    }
}
