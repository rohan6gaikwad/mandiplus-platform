package com.rtech.mandiplus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "buyers")
public class Buyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "representative_name", nullable = false, length = 100)
    private String representativeName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(length = 15)
    private String gstin;

    public Buyer() {
    }

    public Buyer(Long id, User user, String companyName, String representativeName, String phone, String address, String gstin) {
        this.id = id;
        this.user = user;
        this.companyName = companyName;
        this.representativeName = representativeName;
        this.phone = phone;
        this.address = address;
        this.gstin = gstin;
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

    @Transient
    private java.util.List<Object> cartItems = new java.util.ArrayList<>();

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be blank or null");
        }
        this.companyName = companyName.trim();
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public void setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone != null && !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be 10 digits");
        }
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address is mandatory");
        }
        this.address = address;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        if (gstin != null && !gstin.trim().isEmpty() && gstin.trim().length() != 15) {
            throw new IllegalArgumentException("GSTIN must be 15 characters");
        }
        this.gstin = gstin;
    }

    public java.util.List<Object> getCartItems() {
        return cartItems != null ? cartItems : java.util.Collections.emptyList();
    }

    public int getCartItemCount() {
        return getCartItems().size();
    }

    public static Buyer createAndValidate(User user, String companyName, String name, String phone, String address, String gstin) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new RuntimeException("Error: Company name is required.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Error: Representative name is required.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("Error: Phone number is required.");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("Error: Delivery address is required.");
        }

        Buyer buyer = new Buyer();
        buyer.setUser(user);
        buyer.setCompanyName(companyName);
        buyer.setRepresentativeName(name);
        buyer.setPhone(phone);
        buyer.setAddress(address);
        buyer.setGstin(gstin);
        return buyer;
    }

    public void updateDetails(String companyName, String name, String phone, String address, String gstin) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new RuntimeException("Error: Company name is required.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Error: Representative name is required.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("Error: Phone number is required.");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("Error: Delivery address is required.");
        }

        this.companyName = companyName;
        this.representativeName = name;
        this.phone = phone;
        this.address = address;
        this.gstin = gstin;
    }
}
