package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DeliveryTaskDTO {
    private Long id;
    private String taskCode;
    private Long orderId;
    private Long partnerId;
    private String partnerName;
    private String pickupLocation;
    private String pickupContactName;
    private String pickupContactPhone;
    private String dropLocation;
    private String dropContactName;
    private String dropContactPhone;
    private String cropName;
    private String variety;
    private BigDecimal quantityQuintals;
    private BigDecimal payoutAmount;
    private BigDecimal surgeBonus;
    private BigDecimal distanceKm;
    private Integer estimatedMinutes;
    private String status;
    private String deliveryOtp;
    private String paymentType;
    private BigDecimal cashToCollect;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime locationUpdatedAt;
    private Boolean isLiveTrackingActive;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickupConfirmedAt;
    private LocalDateTime transitStartedAt;
    private boolean partnerConfirmed;
    private String partnerPhone;
    private String partnerVehicleNumber;

    public DeliveryTaskDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getPickupContactName() {
        return pickupContactName;
    }

    public void setPickupContactName(String pickupContactName) {
        this.pickupContactName = pickupContactName;
    }

    public String getPickupContactPhone() {
        return pickupContactPhone;
    }

    public void setPickupContactPhone(String pickupContactPhone) {
        this.pickupContactPhone = pickupContactPhone;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public String getDropContactName() {
        return dropContactName;
    }

    public void setDropContactName(String dropContactName) {
        this.dropContactName = dropContactName;
    }

    public String getDropContactPhone() {
        return dropContactPhone;
    }

    public void setDropContactPhone(String dropContactPhone) {
        this.dropContactPhone = dropContactPhone;
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

    public BigDecimal getQuantityQuintals() {
        return quantityQuintals;
    }

    public void setQuantityQuintals(BigDecimal quantityQuintals) {
        this.quantityQuintals = quantityQuintals;
    }

    public BigDecimal getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(BigDecimal payoutAmount) {
        this.payoutAmount = payoutAmount;
    }

    public BigDecimal getSurgeBonus() {
        return surgeBonus;
    }

    public void setSurgeBonus(BigDecimal surgeBonus) {
        this.surgeBonus = surgeBonus;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryOtp() {
        return deliveryOtp;
    }

    public void setDeliveryOtp(String deliveryOtp) {
        this.deliveryOtp = deliveryOtp;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getCashToCollect() {
        return cashToCollect;
    }

    public void setCashToCollect(BigDecimal cashToCollect) {
        this.cashToCollect = cashToCollect;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public LocalDateTime getLocationUpdatedAt() {
        return locationUpdatedAt;
    }

    public void setLocationUpdatedAt(LocalDateTime locationUpdatedAt) {
        this.locationUpdatedAt = locationUpdatedAt;
    }

    public Boolean getIsLiveTrackingActive() {
        return isLiveTrackingActive;
    }

    public void setIsLiveTrackingActive(Boolean isLiveTrackingActive) {
        this.isLiveTrackingActive = isLiveTrackingActive;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getPickupConfirmedAt() {
        return pickupConfirmedAt;
    }

    public void setPickupConfirmedAt(LocalDateTime pickupConfirmedAt) {
        this.pickupConfirmedAt = pickupConfirmedAt;
    }

    public LocalDateTime getTransitStartedAt() {
        return transitStartedAt;
    }

    public void setTransitStartedAt(LocalDateTime transitStartedAt) {
        this.transitStartedAt = transitStartedAt;
    }

    public boolean isPartnerConfirmed() {
        return partnerConfirmed;
    }

    public void setPartnerConfirmed(boolean partnerConfirmed) {
        this.partnerConfirmed = partnerConfirmed;
    }

    public String getPartnerPhone() {
        return partnerPhone;
    }

    public void setPartnerPhone(String partnerPhone) {
        this.partnerPhone = partnerPhone;
    }

    public String getPartnerVehicleNumber() {
        return partnerVehicleNumber;
    }

    public void setPartnerVehicleNumber(String partnerVehicleNumber) {
        this.partnerVehicleNumber = partnerVehicleNumber;
    }
}
