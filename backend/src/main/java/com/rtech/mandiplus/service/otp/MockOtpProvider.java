package com.rtech.mandiplus.service.otp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dedicated Mock OTP Provider for unit and automated testing suites.
 * Records challenges in-memory for test verification.
 */
@Service
@ConditionalOnProperty(name = "mandiplus.otp.provider", havingValue = "mock")
public class MockOtpProvider implements OtpProvider {

    private final Map<String, String> lastSentOtpByPhone = new ConcurrentHashMap<>();
    private boolean simulateFailure = false;

    @Override
    public OtpSendResult sendOtp(String phoneNumber, String otp, int expiryMinutes) {
        if (simulateFailure) {
            return OtpSendResult.failure("Simulated SMS network failure");
        }
        lastSentOtpByPhone.put(phoneNumber, otp);
        return OtpSendResult.success("MOCK-SMS-" + System.currentTimeMillis());
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    public String getLastSentOtp(String phoneNumber) {
        return lastSentOtpByPhone.get(phoneNumber);
    }

    public void clear() {
        lastSentOtpByPhone.clear();
        simulateFailure = false;
    }

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }
}
