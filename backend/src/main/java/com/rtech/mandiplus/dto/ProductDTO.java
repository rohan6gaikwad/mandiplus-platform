package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDTO {
    private Long id;
    private Long farmerId;
    private String farmerName;
    private String farmerPhone;
    private String cropName;
    private String variety;
    private BigDecimal quantityQuintals;
    private BigDecimal pricePerQuintal;
    private String description;
    private String imageUrl;
    private String location;
    private String status;
    private BigDecimal suggestedPricePerQuintal;
    private String adminFeedback;
    private String farmerProfilePhoto;
    private LocalDateTime createdAt;

    public ProductDTO() {
    }

    public ProductDTO(Long id, Long farmerId, String farmerName, String farmerPhone, String cropName,
                      String variety, BigDecimal quantityQuintals, BigDecimal pricePerQuintal,
                      String description, String imageUrl, String location, String status,
                      String farmerProfilePhoto, LocalDateTime createdAt) {
        this.id = id;
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.farmerPhone = farmerPhone;
        this.cropName = cropName;
        this.variety = variety;
        this.quantityQuintals = quantityQuintals;
        this.pricePerQuintal = pricePerQuintal;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        this.status = status;
        this.farmerProfilePhoto = farmerProfilePhoto;
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

    public BigDecimal getPricePerQuintal() {
        return pricePerQuintal;
    }

    public void setPricePerQuintal(BigDecimal pricePerQuintal) {
        this.pricePerQuintal = pricePerQuintal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getSuggestedPricePerQuintal() {
        return suggestedPricePerQuintal;
    }

    public void setSuggestedPricePerQuintal(BigDecimal suggestedPricePerQuintal) {
        this.suggestedPricePerQuintal = suggestedPricePerQuintal;
    }

    public String getAdminFeedback() {
        return adminFeedback;
    }

    public void setAdminFeedback(String adminFeedback) {
        this.adminFeedback = adminFeedback;
    }
}
