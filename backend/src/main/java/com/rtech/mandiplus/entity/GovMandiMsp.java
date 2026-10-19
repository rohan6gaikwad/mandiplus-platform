package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gov_mandi_msp")
public class GovMandiMsp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String commodity;

    @Column(length = 50)
    private String category; // 'Kharif', 'Rabi', etc.

    @Column(name = "msp_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal mspPrice;

    @Column(name = "effective_season", length = 50)
    private String effectiveSeason; // e.g. '2026-27'

    @Column(nullable = false)
    private Boolean verified = true;

    @Column(length = 150)
    private String source; // e.g. 'CACP / Ministry of Agriculture & Farmers Welfare'

    @Column(length = 255)
    private String notes;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public GovMandiMsp() {
    }

    public GovMandiMsp(String commodity, String category, BigDecimal mspPrice, String effectiveSeason, String source) {
        this.commodity = commodity;
        this.category = category;
        this.mspPrice = mspPrice;
        this.effectiveSeason = effectiveSeason;
        this.source = source;
        this.verified = true;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getMspPrice() {
        return mspPrice;
    }

    public void setMspPrice(BigDecimal mspPrice) {
        this.mspPrice = mspPrice;
    }

    public String getEffectiveSeason() {
        return effectiveSeason;
    }

    public void setEffectiveSeason(String effectiveSeason) {
        this.effectiveSeason = effectiveSeason;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
