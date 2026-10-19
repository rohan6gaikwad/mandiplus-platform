package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderDTO {
    private Long id;
    private Long buyerId;
    private String buyerCompanyName;
    private String buyerRepresentativeName;
    private String buyerPhone;
    private String buyerAddress;
    private Long productId;
    private String cropName;
    private String variety;
    private BigDecimal pricePerQuintal;
    private BigDecimal quantityQuintals;
    private BigDecimal totalPrice;
    private String status;
    private LocalDate estimatedDeliveryDate;
    private String buyerProfilePhoto;
    private String farmerName;
    private String farmerPhone;
    private String farmerProfilePhoto;
    private String paymentMethod;
    private String paymentStatus;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;

    public OrderDTO() {
    }

    public OrderDTO(Long id, Long buyerId, String buyerCompanyName, String buyerRepresentativeName,
                    String buyerPhone, String buyerAddress, Long productId, String cropName,
                    String variety, BigDecimal pricePerQuintal, BigDecimal quantityQuintals,
                    BigDecimal totalPrice, String status, LocalDate estimatedDeliveryDate,
                    String buyerProfilePhoto, String farmerName, String farmerPhone,
                    String farmerProfilePhoto, LocalDateTime createdAt) {
        this.id = id;
        this.buyerId = buyerId;
        this.buyerCompanyName = buyerCompanyName;
        this.buyerRepresentativeName = buyerRepresentativeName;
        this.buyerPhone = buyerPhone;
        this.buyerAddress = buyerAddress;
        this.productId = productId;
        this.cropName = cropName;
        this.variety = variety;
        this.pricePerQuintal = pricePerQuintal;
        this.quantityQuintals = quantityQuintals;
        this.totalPrice = totalPrice;
        this.status = status;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.buyerProfilePhoto = buyerProfilePhoto;
        this.farmerName = farmerName;
        this.farmerPhone = farmerPhone;
        this.farmerProfilePhoto = farmerProfilePhoto;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerCompanyName() {
        return buyerCompanyName;
    }

    public void setBuyerCompanyName(String buyerCompanyName) {
        this.buyerCompanyName = buyerCompanyName;
    }

    public String getBuyerRepresentativeName() {
        return buyerRepresentativeName;
    }

    public void setBuyerRepresentativeName(String buyerRepresentativeName) {
        this.buyerRepresentativeName = buyerRepresentativeName;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public String getBuyerAddress() {
        return buyerAddress;
    }

    public void setBuyerAddress(String buyerAddress) {
        this.buyerAddress = buyerAddress;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public BigDecimal getPricePerQuintal() {
        return pricePerQuintal;
    }

    public void setPricePerQuintal(BigDecimal pricePerQuintal) {
        this.pricePerQuintal = pricePerQuintal;
    }

    public BigDecimal getQuantityQuintals() {
        return quantityQuintals;
    }

    public void setQuantityQuintals(BigDecimal quantityQuintals) {
        this.quantityQuintals = quantityQuintals;
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

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getBuyerProfilePhoto() {
        return buyerProfilePhoto;
    }

    public void setBuyerProfilePhoto(String buyerProfilePhoto) {
        this.buyerProfilePhoto = buyerProfilePhoto;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }
}
