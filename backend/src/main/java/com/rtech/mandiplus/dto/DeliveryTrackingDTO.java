package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DeliveryTrackingDTO {

    private Long orderId;
    private Long deliveryTaskId;
    private String taskCode;
    private String cropName;
    private String variety;
    private BigDecimal quantityQuintals;
    private String pickupLocation;
    private String dropLocation;

    private String status;
    private String displayStatus;
    private boolean partnerConfirmed;

    // Delivery Partner Details (Only populated AFTER partner acceptance)
    private Long partnerId;
    private String partnerName;
    private String partnerPhone;
    private String vehicleType;
    private String vehicleNumber;
    private BigDecimal partnerRating;
    private String partnerPhoto;

    // Real GPS Tracking Data
    private boolean isLiveTrackingActive;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime locationUpdatedAt;
    private Integer estimatedMinutes;
    private BigDecimal distanceKm;
    private String trackingWarning;

    // Verification OTP (visible to buyer for fulfillment verification)
    private String deliveryOtp;

    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickupConfirmedAt;
    private LocalDateTime transitStartedAt;
    private LocalDateTime deliveredAt;

    public DeliveryTrackingDTO() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getDeliveryTaskId() {
        return deliveryTaskId;
    }

    public void setDeliveryTaskId(Long deliveryTaskId) {
        this.deliveryTaskId = deliveryTaskId;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
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

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public boolean isPartnerConfirmed() {
        return partnerConfirmed;
    }

    public void setPartnerConfirmed(boolean partnerConfirmed) {
        this.partnerConfirmed = partnerConfirmed;
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

    public String getPartnerPhone() {
        return partnerPhone;
    }

    public void setPartnerPhone(String partnerPhone) {
        this.partnerPhone = partnerPhone;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public BigDecimal getPartnerRating() {
        return partnerRating;
    }

    public void setPartnerRating(BigDecimal partnerRating) {
        this.partnerRating = partnerRating;
    }

    public String getPartnerPhoto() {
        return partnerPhoto;
    }

    public void setPartnerPhoto(String partnerPhoto) {
        this.partnerPhoto = partnerPhoto;
    }

    public boolean isLiveTrackingActive() {
        return isLiveTrackingActive;
    }

    public void setLiveTrackingActive(boolean liveTrackingActive) {
        isLiveTrackingActive = liveTrackingActive;
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

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getTrackingWarning() {
        return trackingWarning;
    }

    public void setTrackingWarning(String trackingWarning) {
        this.trackingWarning = trackingWarning;
    }

    public String getDeliveryOtp() {
        return deliveryOtp;
    }

    public void setDeliveryOtp(String deliveryOtp) {
        this.deliveryOtp = deliveryOtp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}
