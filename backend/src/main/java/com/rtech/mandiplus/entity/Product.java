package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(length = 100)
    private String variety;

    @Column(name = "quantity_quintals", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityQuintals;

    @Column(name = "price_per_quintal", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerQuintal;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(name = "suggested_price_per_quintal", precision = 10, scale = 2)
    private BigDecimal suggestedPricePerQuintal;

    @Column(name = "admin_feedback", columnDefinition = "TEXT")
    private String adminFeedback;

    @Column(length = 50)
    private String status = "PENDING_APPROVAL"; // 'PENDING_APPROVAL', 'AVAILABLE', 'REJECTED', 'SOLD'

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(Long id, Farmer farmer, String cropName, String variety,
                   BigDecimal quantityQuintals, BigDecimal pricePerQuintal,
                   String description, String imageUrl, String location,
                   String status, LocalDateTime createdAt) {
        this.id = id;
        this.farmer = farmer;
        this.cropName = cropName;
        this.variety = variety;
        this.quantityQuintals = quantityQuintals;
        this.pricePerQuintal = pricePerQuintal;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    public void setFarmer(Farmer farmer) {
        this.farmer = farmer;
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

    public void validateAvailability(BigDecimal quantity) {
        if (!"AVAILABLE".equals(this.status)) {
            throw new RuntimeException("Error: Product is no longer available!");
        }
        if (this.quantityQuintals.compareTo(quantity) < 0) {
            throw new RuntimeException("Error: Insufficient quantity available!");
        }
    }

    public void deductQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Error: Invalid deduction quantity!");
        }
        validateAvailability(quantity);
        this.quantityQuintals = this.quantityQuintals.subtract(quantity);
        if (this.quantityQuintals.compareTo(BigDecimal.ZERO) == 0) {
            this.status = "SOLD";
        }
    }

    public void deductQuantity(double quantity) {
        deductQuantity(BigDecimal.valueOf(quantity));
    }

    public void restoreQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Error: Quantity to restore must be greater than zero!");
        }
        this.quantityQuintals = this.quantityQuintals.add(quantity);
        if ("SOLD".equals(this.status)) {
            this.status = "AVAILABLE";
        }
    }

    public void restoreQuantity(double quantity) {
        restoreQuantity(BigDecimal.valueOf(quantity));
    }

    public void updateDetails(String cropName, String variety, BigDecimal quantity, BigDecimal price, String description, String imageUrl, String location, String targetStatus) {
        this.cropName = cropName;
        this.variety = variety;
        this.quantityQuintals = quantity;
        this.pricePerQuintal = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        if (targetStatus != null) {
            this.status = targetStatus;
        }
    }
}
