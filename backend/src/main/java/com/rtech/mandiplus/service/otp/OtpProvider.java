package com.rtech.mandiplus.service.otp;

public interface OtpProvider {
    /**
     * Dispatch OTP to Indian mobile number.
     * @param phoneNumber 10-digit Indian phone number
     * @param otp 6-digit numeric OTP challenge
     * @param expiryMinutes Validity in minutes
     * @return OtpSendResult
     */
    OtpSendResult sendOtp(String phoneNumber, String otp, int expiryMinutes);

    String getProviderName();
}
