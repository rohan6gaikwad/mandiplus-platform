package com.rtech.mandiplus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class RegisterRequest {
    @NotBlank
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    private String role; // 'FARMER', 'BUYER', 'COLD_STORAGE_MANAGER', 'ADMIN'

    private String profilePhoto;

    // Farmer Profile Fields
    private String name; // Also used for representative name
    private String phone;
    private String state;
    private String district;
    private BigDecimal farmSizeAcres;
    private String upiId;

    // Buyer Profile Fields
    private String companyName;
    private String address;
    private String gstin;

    // Cold Storage Facility Initial Creation Fields
    private String storageName;
    private String storageLocation;
    private BigDecimal storageCapacityTons;
    private BigDecimal storagePricePerTonDay;
    private String storageDescription;

    // Delivery Partner Fields
    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private String preferredHub;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String role, String profilePhoto,
                           String name, String phone, String state, String district,
                           BigDecimal farmSizeAcres, String upiId, String companyName,
                           String address, String gstin, String storageName,
                           String storageLocation, BigDecimal storageCapacityTons,
                           BigDecimal storagePricePerTonDay, String storageDescription) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.profilePhoto = profilePhoto;
        this.name = name;
        this.phone = phone;
        this.state = state;
        this.district = district;
        this.farmSizeAcres = farmSizeAcres;
        this.upiId = upiId;
        this.companyName = companyName;
        this.address = address;
        this.gstin = gstin;
        this.storageName = storageName;
        this.storageLocation = storageLocation;
        this.storageCapacityTons = storageCapacityTons;
        this.storagePricePerTonDay = storagePricePerTonDay;
        this.storageDescription = storageDescription;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public BigDecimal getFarmSizeAcres() {
        return farmSizeAcres;
    }

    public void setFarmSizeAcres(BigDecimal farmSizeAcres) {
        this.farmSizeAcres = farmSizeAcres;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
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

    public BigDecimal getStorageCapacityTons() {
        return storageCapacityTons;
    }

    public void setStorageCapacityTons(BigDecimal storageCapacityTons) {
        this.storageCapacityTons = storageCapacityTons;
    }

    public BigDecimal getStoragePricePerTonDay() {
        return storagePricePerTonDay;
    }

    public void setStoragePricePerTonDay(BigDecimal storagePricePerTonDay) {
        this.storagePricePerTonDay = storagePricePerTonDay;
    }

    public String getStorageDescription() {
        return storageDescription;
    }

    public void setStorageDescription(String storageDescription) {
        this.storageDescription = storageDescription;
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

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getPreferredHub() {
        return preferredHub;
    }

    public void setPreferredHub(String preferredHub) {
        this.preferredHub = preferredHub;
    }
}
