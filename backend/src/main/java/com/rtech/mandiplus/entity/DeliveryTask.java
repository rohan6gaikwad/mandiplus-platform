package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_tasks")
public class DeliveryTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", nullable = false, unique = true, length = 50)
    private String taskCode;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner;

    @Column(name = "pickup_location", nullable = false, length = 200)
    private String pickupLocation;

    @Column(name = "pickup_contact_name", length = 100)
    private String pickupContactName;

    @Column(name = "pickup_contact_phone", length = 20)
    private String pickupContactPhone;

    @Column(name = "drop_location", nullable = false, length = 200)
    private String dropLocation;

    @Column(name = "drop_contact_name", length = 100)
    private String dropContactName;

    @Column(name = "drop_contact_phone", length = 20)
    private String dropContactPhone;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(length = 100)
    private String variety;

    @Column(name = "quantity_quintals", precision = 10, scale = 2)
    private BigDecimal quantityQuintals;

    @Column(name = "payout_amount", precision = 10, scale = 2)
    private BigDecimal payoutAmount;

    @Column(name = "surge_bonus", precision = 10, scale = 2)
    private BigDecimal surgeBonus = BigDecimal.ZERO;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(length = 50)
    private String status = "AVAILABLE"; // 'AVAILABLE', 'ASSIGNED', 'REACHED_PICKUP', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'

    @Column(name = "delivery_otp", length = 10)
    private String deliveryOtp = "5821";

    @Column(name = "payment_type", length = 50)
    private String paymentType = "ESCROW_PAID"; // 'ESCROW_PAID', 'CASH_ON_DELIVERY'

    @Column(name = "cash_to_collect", precision = 10, scale = 2)
    private BigDecimal cashToCollect = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;

    @Column(name = "is_live_tracking_active")
    private Boolean isLiveTrackingActive = false;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "pickup_confirmed_at")
    private LocalDateTime pickupConfirmedAt;

    @Column(name = "transit_started_at")
    private LocalDateTime transitStartedAt;

    public DeliveryTask() {
    }

    public DeliveryTask(Long id, String taskCode, Order order, DeliveryPartner deliveryPartner,
                        String pickupLocation, String pickupContactName, String pickupContactPhone,
                        String dropLocation, String dropContactName, String dropContactPhone,
                        String cropName, String variety, BigDecimal quantityQuintals,
                        BigDecimal payoutAmount, BigDecimal surgeBonus, BigDecimal distanceKm,
                        Integer estimatedMinutes, String status, String deliveryOtp,
                        String paymentType, BigDecimal cashToCollect, LocalDateTime createdAt,
                        LocalDateTime deliveredAt) {
        this.id = id;
        this.taskCode = taskCode;
        this.order = order;
        this.deliveryPartner = deliveryPartner;
        this.pickupLocation = pickupLocation;
        this.pickupContactName = pickupContactName;
        this.pickupContactPhone = pickupContactPhone;
        this.dropLocation = dropLocation;
        this.dropContactName = dropContactName;
        this.dropContactPhone = dropContactPhone;
        this.cropName = cropName;
        this.variety = variety;
        this.quantityQuintals = quantityQuintals;
        this.payoutAmount = payoutAmount;
        this.surgeBonus = surgeBonus != null ? surgeBonus : BigDecimal.ZERO;
        this.distanceKm = distanceKm;
        this.estimatedMinutes = estimatedMinutes;
        this.status = status != null ? status : "AVAILABLE";
        this.deliveryOtp = deliveryOtp != null ? deliveryOtp : "5821";
        this.paymentType = paymentType != null ? paymentType : "ESCROW_PAID";
        this.cashToCollect = cashToCollect != null ? cashToCollect : BigDecimal.ZERO;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.deliveredAt = deliveredAt;
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

    public Order getOrder() {
        return order;
    }

    public Long getOrderId() {
        return order != null ? order.getId() : null;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public DeliveryPartner getDeliveryPartner() {
        return deliveryPartner;
    }

    public void setDeliveryPartner(DeliveryPartner deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
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

    /**
     * Checks whether an authenticated delivery partner has explicitly accepted this consignment.
     */
    public boolean isPartnerConfirmed() {
        if (this.deliveryPartner == null || this.status == null) {
            return false;
        }
        String s = this.status.toUpperCase();
        return "DELIVERY_ACCEPTED".equals(s) ||
               "PICKUP_CONFIRMED".equals(s) ||
               "PICKED_UP".equals(s) ||
               "REACHED_PICKUP".equals(s) ||
               "IN_TRANSIT".equals(s) ||
               "DELIVERED".equals(s);
    }

    /**
     * Live GPS tracking is strictly active only when partner has confirmed acceptance,
     * the task is actively in pickup/transit (not delivered/cancelled),
     * live tracking flag is active, and real GPS coordinates have been reported.
     */
    public boolean isLiveGpsActive() {
        if (!isPartnerConfirmed() || this.deliveryPartner == null) {
            return false;
        }
        String s = this.status.toUpperCase();
        if ("DELIVERED".equals(s) || "CANCELLED".equals(s)) {
            return false;
        }
        return Boolean.TRUE.equals(this.isLiveTrackingActive) &&
               this.currentLatitude != null &&
               this.currentLongitude != null &&
               this.locationUpdatedAt != null;
    }
}
