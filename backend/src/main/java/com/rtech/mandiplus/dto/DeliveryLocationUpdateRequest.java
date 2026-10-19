package com.rtech.mandiplus.dto;

import jakarta.validation.constraints.NotNull;

public class DeliveryLocationUpdateRequest {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private Double accuracyMeters;
    private Double speedKmh;
    private Double headingDegrees;
    private Boolean isPermissionGranted = true;

    public DeliveryLocationUpdateRequest() {
    }

    public DeliveryLocationUpdateRequest(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isPermissionGranted = true;
    }

    public DeliveryLocationUpdateRequest(Double latitude, Double longitude, Boolean isPermissionGranted) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isPermissionGranted = isPermissionGranted;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracyMeters() {
        return accuracyMeters;
    }

    public void setAccuracyMeters(Double accuracyMeters) {
        this.accuracyMeters = accuracyMeters;
    }

    public Double getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(Double speedKmh) {
        this.speedKmh = speedKmh;
    }

    public Double getHeadingDegrees() {
        return headingDegrees;
    }

    public void setHeadingDegrees(Double headingDegrees) {
        this.headingDegrees = headingDegrees;
    }

    public Boolean getIsPermissionGranted() {
        return isPermissionGranted;
    }

    public void setIsPermissionGranted(Boolean isPermissionGranted) {
        this.isPermissionGranted = isPermissionGranted;
    }
}
