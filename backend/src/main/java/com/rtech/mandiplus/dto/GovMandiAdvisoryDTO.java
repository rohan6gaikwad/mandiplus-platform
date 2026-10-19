package com.rtech.mandiplus.dto;

import java.time.LocalDateTime;

public class GovMandiAdvisoryDTO {

    private Long id;
    private String commodity;
    private String district;
    private String title;
    private String advisoryText;
    private String alertLevel;
    private Boolean active;
    private LocalDateTime createdAt;
    private String createdBy;

    public GovMandiAdvisoryDTO() {
    }

    public GovMandiAdvisoryDTO(Long id, String commodity, String district, String title,
                               String advisoryText, String alertLevel, Boolean active,
                               LocalDateTime createdAt, String createdBy) {
        this.id = id;
        this.commodity = commodity;
        this.district = district;
        this.title = title;
        this.advisoryText = advisoryText;
        this.alertLevel = alertLevel;
        this.active = active;
        this.createdAt = createdAt;
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
