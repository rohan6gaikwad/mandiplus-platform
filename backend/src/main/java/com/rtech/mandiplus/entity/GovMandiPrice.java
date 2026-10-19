package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "gov_mandi_prices",
    indexes = {
        @Index(name = "idx_gov_mandi_comm_dist", columnList = "commodity, district, market"),
        @Index(name = "idx_gov_mandi_arrival", columnList = "arrival_date"),
        @Index(name = "idx_gov_mandi_state_comm", columnList = "state, commodity")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_gov_mandi_arrival",
            columnNames = {"state", "district", "market", "commodity", "variety", "grade", "arrival_date"}
        )
    }
)
public class GovMandiPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 150)
    private String market;

    @Column(nullable = false, length = 100)
    private String commodity;

    @Column(length = 100)
    private String variety;

    @Column(length = 50)
    private String grade;

    @Column(name = "arrival_date", nullable = false)
    private LocalDate arrivalDate;

    @Column(name = "min_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "modal_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal modalPrice;

    @Column(name = "raw_source", length = 150)
    private String rawSource;

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;

    public GovMandiPrice() {
    }

    public GovMandiPrice(String state, String district, String market, String commodity,
                         String variety, String grade, LocalDate arrivalDate,
                         BigDecimal minPrice, BigDecimal maxPrice, BigDecimal modalPrice,
                         String rawSource) {
        this.state = state;
        this.district = district;
        this.market = market;
        this.commodity = commodity;
        this.variety = variety;
        this.grade = grade;
        this.arrivalDate = arrivalDate;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.modalPrice = modalPrice;
        this.rawSource = rawSource;
        this.importedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
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

    public String getRawSource() {
        return rawSource;
    }

    public void setRawSource(String rawSource) {
        this.rawSource = rawSource;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }
}
