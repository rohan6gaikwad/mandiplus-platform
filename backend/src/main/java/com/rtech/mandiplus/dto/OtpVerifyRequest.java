package com.rtech.mandiplus.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpVerifyRequest {

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "OTP code is required")
    private String otp;

    private String role;

    public OtpVerifyRequest() {}

    public OtpVerifyRequest(String phone, String otp) {
        this.phone = phone;
        this.otp = otp;
    }

    public OtpVerifyRequest(String phone, String otp, String role) {
        this.phone = phone;
        this.otp = otp;
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
