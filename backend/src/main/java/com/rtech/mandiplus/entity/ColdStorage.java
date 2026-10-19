package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

@Entity
@Table(name = "cold_storage")
public class ColdStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User manager; // Managed by user with role 'COLD_STORAGE_MANAGER'

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(name = "total_capacity_tons", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCapacityTons;

    @Column(name = "available_capacity_tons", nullable = false, precision = 10, scale = 2)
    private BigDecimal availableCapacityTons;

    @Column(name = "price_per_ton_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerTonDay;

    @Column(columnDefinition = "TEXT")
    private String description;

    public ColdStorage() {
    }

    public ColdStorage(Long id, User manager, String name, String location, BigDecimal totalCapacityTons,
                       BigDecimal availableCapacityTons, BigDecimal pricePerTonDay, String description) {
        this.id = id;
        this.manager = manager;
        this.name = name;
        this.location = location;
        this.totalCapacityTons = totalCapacityTons;
        this.availableCapacityTons = availableCapacityTons;
        this.pricePerTonDay = pricePerTonDay;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getTotalCapacityTons() {
        return totalCapacityTons;
    }

    public void setTotalCapacityTons(BigDecimal totalCapacityTons) {
        this.totalCapacityTons = totalCapacityTons;
    }

    public BigDecimal getAvailableCapacityTons() {
        return availableCapacityTons;
    }

    public void setAvailableCapacityTons(BigDecimal availableCapacityTons) {
        this.availableCapacityTons = availableCapacityTons;
    }

    public BigDecimal getPricePerTonDay() {
        return pricePerTonDay;
    }

    public void setPricePerTonDay(BigDecimal pricePerTonDay) {
        this.pricePerTonDay = pricePerTonDay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void allocateCapacity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Allocation capacity must be positive!");
        }
        BigDecimal remaining = this.availableCapacityTons.subtract(amount);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Error: Storage is overbooked!");
        }
        this.availableCapacityTons = remaining;
    }

    public void allocateCapacity(double amount) {
        allocateCapacity(BigDecimal.valueOf(amount));
    }

    public void restoreCapacity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Restoration capacity must be positive!");
        }
        BigDecimal restored = this.availableCapacityTons.add(amount);
        if (restored.compareTo(this.totalCapacityTons) > 0) {
            restored = this.totalCapacityTons;
        }
        this.availableCapacityTons = restored;
    }

    public void restoreCapacity(double amount) {
        restoreCapacity(BigDecimal.valueOf(amount));
    }

    public static ColdStorage createAndValidate(User manager, String name, String location, BigDecimal totalCapacityTons, BigDecimal pricePerTonDay, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Error: Storage facility name is required.");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new RuntimeException("Error: Storage location is required.");
        }
        if (totalCapacityTons == null || totalCapacityTons.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Error: Storage capacity must be a positive number.");
        }
        if (pricePerTonDay == null || pricePerTonDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Error: Rental rate per ton/day must be a positive number.");
        }

        ColdStorage storage = new ColdStorage();
        storage.setManager(manager);
        storage.setName(name);
        storage.setLocation(location);
        storage.setTotalCapacityTons(totalCapacityTons);
        storage.setAvailableCapacityTons(totalCapacityTons);
        storage.setPricePerTonDay(pricePerTonDay);
        storage.setDescription(description != null ? description : "IoT Temperature Controlled Space.");
        return storage;
    }

    public void updateDetails(String name, String location, BigDecimal totalCapacityTons, BigDecimal pricePerTonDay, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Error: Storage facility name is required.");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new RuntimeException("Error: Storage location is required.");
        }
        if (totalCapacityTons == null || totalCapacityTons.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Error: Storage capacity must be a positive number.");
        }
        if (pricePerTonDay == null || pricePerTonDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Error: Rental rate per ton/day must be a positive number.");
        }

        BigDecimal diff = totalCapacityTons.subtract(this.totalCapacityTons);
        this.totalCapacityTons = totalCapacityTons;
        
        BigDecimal remaining = this.availableCapacityTons.add(diff);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            this.availableCapacityTons = BigDecimal.ZERO;
        } else {
            this.availableCapacityTons = remaining;
        }

        this.name = name;
        this.location = location;
        this.pricePerTonDay = pricePerTonDay;
        this.description = description;
    }
}
