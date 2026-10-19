package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 50)
    private String city = "Nashik";

    @Column(length = 50)
    private String district = "Nashik";

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType = "BIKE"; // 'BIKE', 'SCOOTER', 'TEMPO', 'MINI_TRUCK'

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "pan_or_aadhaar", length = 50)
    private String panOrAadhaar;

    @Column(name = "preferred_hub", length = 100)
    private String preferredHub = "Nashik APMC Main Terminal";

    @Column(name = "upi_id", length = 100)
    private String upiId;

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = true;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("4.92");

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column(name = "today_deliveries")
    private Integer todayDeliveries = 0;

    @Column(name = "today_earnings", precision = 10, scale = 2)
    private BigDecimal todayEarnings = BigDecimal.ZERO;

    @Column(name = "total_earnings", precision = 10, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "duty_shift", length = 30)
    private String dutyShift = "FLEXI";

    @Column(length = 30)
    private String status = "ACTIVE"; // 'ACTIVE', 'PENDING_VERIFICATION', 'SUSPENDED'

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public DeliveryPartner() {
    }

    public DeliveryPartner(Long id, User user, String name, String phone, String city, String district,
                           String vehicleType, String vehicleNumber, String licenseNumber,
                           String panOrAadhaar, String preferredHub, String upiId,
                           String emergencyContactName, String emergencyContactPhone,
                           Boolean isOnline, BigDecimal rating, Integer totalDeliveries,
                           Integer todayDeliveries, BigDecimal todayEarnings, BigDecimal totalEarnings,
                           String dutyShift, String status, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.phone = phone;
        this.city = city;
        this.district = district;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.licenseNumber = licenseNumber;
        this.panOrAadhaar = panOrAadhaar;
        this.preferredHub = preferredHub;
        this.upiId = upiId;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.isOnline = isOnline != null ? isOnline : true;
        this.rating = rating != null ? rating : new BigDecimal("4.92");
        this.totalDeliveries = totalDeliveries != null ? totalDeliveries : 0;
        this.todayDeliveries = todayDeliveries != null ? todayDeliveries : 0;
        this.todayEarnings = todayEarnings != null ? todayEarnings : BigDecimal.ZERO;
        this.totalEarnings = totalEarnings != null ? totalEarnings : BigDecimal.ZERO;
        this.dutyShift = dutyShift != null ? dutyShift : "FLEXI";
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt;
    }

    public static DeliveryPartner create(User user, String name, String phone, String district,
                                         String vehicleType, String vehicleNumber, String licenseNumber,
                                         String preferredHub, String upiId) {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setUser(user);
        partner.setName(name != null && !name.trim().isEmpty() ? name.trim() : "Delivery Partner");
        partner.setPhone(phone != null ? phone.trim() : "");
        partner.setDistrict(district != null && !district.trim().isEmpty() ? district.trim() : "Nashik");
        partner.setCity(partner.getDistrict());
        partner.setVehicleType(vehicleType != null && !vehicleType.trim().isEmpty() ? vehicleType.trim() : "BIKE");
        partner.setVehicleNumber(vehicleNumber != null ? vehicleNumber.trim() : "MH-15-EXPRESS");
        partner.setLicenseNumber(licenseNumber != null ? licenseNumber.trim() : "MH15202400987");
        partner.setPreferredHub(preferredHub != null && !preferredHub.trim().isEmpty() ? preferredHub.trim() : "Nashik APMC Main Terminal");
        partner.setUpiId(upiId != null ? upiId.trim() : "");
        partner.setIsOnline(true);
        partner.setRating(new BigDecimal("4.92"));
        partner.setTotalDeliveries(14);
        partner.setTodayDeliveries(3);
        partner.setTodayEarnings(new BigDecimal("1240.00"));
        partner.setTotalEarnings(new BigDecimal("18650.00"));
        partner.setStatus("ACTIVE");
        return partner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
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

    public String getPanOrAadhaar() {
        return panOrAadhaar;
    }

    public void setPanOrAadhaar(String panOrAadhaar) {
        this.panOrAadhaar = panOrAadhaar;
    }

    public String getPreferredHub() {
        return preferredHub;
    }

    public void setPreferredHub(String preferredHub) {
        this.preferredHub = preferredHub;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean online) {
        isOnline = online;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getTotalDeliveries() {
        return totalDeliveries;
    }

    public void setTotalDeliveries(Integer totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }

    public Integer getTodayDeliveries() {
        return todayDeliveries;
    }

    public void setTodayDeliveries(Integer todayDeliveries) {
        this.todayDeliveries = todayDeliveries;
    }

    public BigDecimal getTodayEarnings() {
        return todayEarnings;
    }

    public void setTodayEarnings(BigDecimal todayEarnings) {
        this.todayEarnings = todayEarnings;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public String getDutyShift() {
        return dutyShift;
    }

    public void setDutyShift(String dutyShift) {
        this.dutyShift = dutyShift;
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

    public void addCompletedTrip(BigDecimal earnings) {
        this.todayDeliveries = (this.todayDeliveries != null ? this.todayDeliveries : 0) + 1;
        this.totalDeliveries = (this.totalDeliveries != null ? this.totalDeliveries : 0) + 1;
        BigDecimal amt = earnings != null ? earnings : BigDecimal.ZERO;
        this.todayEarnings = (this.todayEarnings != null ? this.todayEarnings : BigDecimal.ZERO).add(amt);
        this.totalEarnings = (this.totalEarnings != null ? this.totalEarnings : BigDecimal.ZERO).add(amt);
    }

    public void addCompletedTrip(double earnings) {
        addCompletedTrip(BigDecimal.valueOf(earnings));
    }

    public BigDecimal getWalletBalance() {
        return this.totalEarnings != null ? this.totalEarnings : BigDecimal.ZERO;
    }

    public Integer getTrips() {
        return this.totalDeliveries != null ? this.totalDeliveries : 0;
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid amount");
        }
        if (this.totalEarnings == null || this.totalEarnings.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }
        this.totalEarnings = this.totalEarnings.subtract(amount);
    }

    public void withdraw(double amount) {
        withdraw(BigDecimal.valueOf(amount));
    }
}
