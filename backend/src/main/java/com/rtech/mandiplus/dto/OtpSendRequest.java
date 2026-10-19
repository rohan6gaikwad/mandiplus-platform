package com.rtech.mandiplus.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpSendRequest {

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String role; // optional for new registration

    public OtpSendRequest() {}

    public OtpSendRequest(String phone) {
        this.phone = phone;
    }

    public OtpSendRequest(String phone, String role) {
        this.phone = phone;
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
