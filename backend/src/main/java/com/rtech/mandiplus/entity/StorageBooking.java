package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage_bookings")
public class StorageBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = true)
    private Farmer farmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = true)
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cold_storage_id", nullable = false)
    private ColdStorage coldStorage;

    @Column(name = "quantity_tons", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityTons;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 50)
    private String status = "PENDING"; // 'PENDING', 'APPROVED', 'ACTIVE', 'COMPLETED', 'REJECTED'

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public StorageBooking() {
    }

    public StorageBooking(Long id, Farmer farmer, Buyer buyer, ColdStorage coldStorage,
                          BigDecimal quantityTons, LocalDate startDate, LocalDate endDate,
                          BigDecimal totalPrice, String status, String rejectionReason,
                          LocalDateTime createdAt) {
        this.id = id;
        this.farmer = farmer;
        this.buyer = buyer;
        this.coldStorage = coldStorage;
        this.quantityTons = quantityTons;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.rejectionReason = rejectionReason;
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

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public ColdStorage getColdStorage() {
        return coldStorage;
    }

    public void setColdStorage(ColdStorage coldStorage) {
        this.coldStorage = coldStorage;
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
