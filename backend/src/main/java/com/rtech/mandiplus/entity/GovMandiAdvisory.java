package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gov_mandi_advisories")
public class GovMandiAdvisory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String commodity;

    @Column(length = 100)
    private String district; // null means all districts

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "advisory_text", nullable = false, length = 2000)
    private String advisoryText;

    @Column(name = "alert_level", nullable = false, length = 30)
    private String alertLevel; // 'INFO', 'WARNING', 'OPPORTUNITY'

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public GovMandiAdvisory() {
    }

    public GovMandiAdvisory(String commodity, String district, String title, String advisoryText, String alertLevel, String createdBy) {
        this.commodity = commodity;
        this.district = district;
        this.title = title;
        this.advisoryText = advisoryText;
        this.alertLevel = alertLevel;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAdvisoryText() {
        return advisoryText;
    }

    public void setAdvisoryText(String advisoryText) {
        this.advisoryText = advisoryText;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
