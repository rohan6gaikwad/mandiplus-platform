package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "farmers")
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(nullable = false, length = 50)
    private String district;

    @Column(name = "farm_size_acres", precision = 10, scale = 2)
    private BigDecimal farmSizeAcres = BigDecimal.ZERO;

    @Column(name = "upi_id", nullable = false, length = 100)
    private String upiId;

    public Farmer() {
    }

    public Farmer(Long id, User user, String name, String phone, String state, String district, BigDecimal farmSizeAcres, String upiId) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.phone = phone;
        this.state = state;
        this.district = district;
        this.farmSizeAcres = farmSizeAcres != null ? farmSizeAcres : BigDecimal.ZERO;
        this.upiId = upiId;
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

    @Column(name = "total_earnings", precision = 12, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Farmer name cannot be null or blank");
        }
        this.name = name.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone != null && !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be a valid 10-digit number");
        }
        this.phone = phone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("State is mandatory");
        }
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        if (district == null || district.trim().isEmpty()) {
            throw new IllegalArgumentException("District is mandatory");
        }
        this.district = district;
    }

    public BigDecimal getFarmSizeAcres() {
        return farmSizeAcres;
    }

    public void setFarmSizeAcres(BigDecimal farmSizeAcres) {
        if (farmSizeAcres != null && farmSizeAcres.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Farm size cannot be negative");
        }
        this.farmSizeAcres = farmSizeAcres;
    }

    public void setFarmSizeAcres(double acres) {
        setFarmSizeAcres(BigDecimal.valueOf(acres));
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        if (upiId == null || upiId.trim().isEmpty()) {
            throw new IllegalArgumentException("UPI ID cannot be blank");
        }
        this.upiId = upiId;
    }

    public BigDecimal getEarnings() {
        return totalEarnings != null ? totalEarnings : BigDecimal.ZERO;
    }

    public BigDecimal getTotalEarnings() {
        return getEarnings();
    }

    public void setEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public void updateDetails(String name, String phone, String state, String district, BigDecimal farmSize, String upiId) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Error: Farmer name is required.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("Error: Contact phone number is required.");
        }
        if (state == null || state.trim().isEmpty()) {
            throw new RuntimeException("Error: State location is required.");
        }
        if (district == null || district.trim().isEmpty()) {
            throw new RuntimeException("Error: District/city location is required.");
        }
        if (upiId == null || upiId.trim().isEmpty()) {
            throw new RuntimeException("Error: UPI ID is required for payments.");
        }

        this.name = name;
        this.phone = phone;
        this.state = state;
        this.district = district;
        this.farmSizeAcres = farmSize != null ? farmSize : BigDecimal.ZERO;
        this.upiId = upiId;
    }

    public static Farmer createAndValidate(User user, String name, String phone, String state, String district, BigDecimal farmSizeAcres, String upiId) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Error: Farmer name is required.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("Error: Contact phone number is required.");
        }
        if (state == null || state.trim().isEmpty()) {
            throw new RuntimeException("Error: State location is required.");
        }
        if (district == null || district.trim().isEmpty()) {
            throw new RuntimeException("Error: District/city location is required.");
        }
        if (upiId == null || upiId.trim().isEmpty()) {
            throw new RuntimeException("Error: UPI ID is required for payments.");
        }

        Farmer farmer = new Farmer();
        farmer.setUser(user);
        farmer.setName(name);
        farmer.setPhone(phone);
        farmer.setState(state);
        farmer.setDistrict(district);
        farmer.setFarmSizeAcres(farmSizeAcres != null ? farmSizeAcres : BigDecimal.ZERO);
        farmer.setUpiId(upiId);
        return farmer;
    }
}
