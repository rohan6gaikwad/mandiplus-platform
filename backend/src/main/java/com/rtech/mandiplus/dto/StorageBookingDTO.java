package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StorageBookingDTO {
    private Long id;
    private Long farmerId;
    private String farmerName;
    private String farmerPhone;
    private Long buyerId;
    private String buyerName;
    private String buyerPhone;
    private Long coldStorageId;
    private String storageName;
    private String storageLocation;
    private BigDecimal quantityTons;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private String status;
    private String farmerProfilePhoto;
    private String buyerProfilePhoto;
    private String rejectionReason;
    private LocalDateTime createdAt;

    public StorageBookingDTO() {
    }

    public StorageBookingDTO(Long id, Long farmerId, String farmerName, String farmerPhone,
                             Long buyerId, String buyerName, String buyerPhone,
                             Long coldStorageId, String storageName, String storageLocation,
                             BigDecimal quantityTons, LocalDate startDate, LocalDate endDate,
                             BigDecimal totalPrice, String status, String farmerProfilePhoto,
                             String buyerProfilePhoto, String rejectionReason,
                             LocalDateTime createdAt) {
        this.id = id;
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.farmerPhone = farmerPhone;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.coldStorageId = coldStorageId;
        this.storageName = storageName;
        this.storageLocation = storageLocation;
        this.quantityTons = quantityTons;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.farmerProfilePhoto = farmerProfilePhoto;
        this.buyerProfilePhoto = buyerProfilePhoto;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmerPhone() {
        return farmerPhone;
    }

    public void setFarmerPhone(String farmerPhone) {
        this.farmerPhone = farmerPhone;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public Long getColdStorageId() {
        return coldStorageId;
    }

    public void setColdStorageId(Long coldStorageId) {
        this.coldStorageId = coldStorageId;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public BigDecimal getQuantityTons() {
        return quantityTons;
    }

    public void setQuantityTons(BigDecimal quantityTons) {
        this.quantityTons = quantityTons;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFarmerProfilePhoto() {
        return farmerProfilePhoto;
    }

    public void setFarmerProfilePhoto(String farmerProfilePhoto) {
        this.farmerProfilePhoto = farmerProfilePhoto;
    }

    public String getBuyerProfilePhoto() {
        return buyerProfilePhoto;
    }

    public void setBuyerProfilePhoto(String buyerProfilePhoto) {
        this.buyerProfilePhoto = buyerProfilePhoto;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
