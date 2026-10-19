package com.rtech.mandiplus.dto;

public class OtpResponseDTO {
    private boolean success;
    private String message;
    private String maskedPhone;
    private int cooldownSeconds;
    private int expiresInSeconds;
    private String previewOtp;
    private boolean realSmsDelivered;

    public OtpResponseDTO() {}

    public OtpResponseDTO(boolean success, String message, String maskedPhone, int cooldownSeconds, int expiresInSeconds) {
        this.success = success;
        this.message = message;
        this.maskedPhone = maskedPhone;
        this.cooldownSeconds = cooldownSeconds;
        this.expiresInSeconds = expiresInSeconds;
    }

    public OtpResponseDTO(boolean success, String message, String maskedPhone, int cooldownSeconds, int expiresInSeconds, String previewOtp, boolean realSmsDelivered) {
        this.success = success;
        this.message = message;
        this.maskedPhone = maskedPhone;
        this.cooldownSeconds = cooldownSeconds;
        this.expiresInSeconds = expiresInSeconds;
        this.previewOtp = previewOtp;
        this.realSmsDelivered = realSmsDelivered;
    }

    public static OtpResponseDTO success(String maskedPhone, int cooldownSeconds, int expiresInSeconds) {
        return new OtpResponseDTO(
                true,
                "OTP challenge dispatched successfully. Please check your phone for the code.",
                maskedPhone,
                cooldownSeconds,
                expiresInSeconds
        );
    }

    public static OtpResponseDTO success(String maskedPhone, int cooldownSeconds, int expiresInSeconds, String previewOtp, boolean realSmsDelivered, String customMsg) {
        return new OtpResponseDTO(
                true,
                customMsg != null ? customMsg : "OTP challenge dispatched successfully. Please check your phone for the code.",
                maskedPhone,
                cooldownSeconds,
                expiresInSeconds,
                previewOtp,
                realSmsDelivered
        );
    }

    public static OtpResponseDTO failure(String message) {
        return new OtpResponseDTO(false, message, null, 0, 0);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMaskedPhone() {
        return maskedPhone;
    }

    public void setMaskedPhone(String maskedPhone) {
        this.maskedPhone = maskedPhone;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(int expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getPreviewOtp() {
        return previewOtp;
    }

    public void setPreviewOtp(String previewOtp) {
        this.previewOtp = previewOtp;
    }

    public boolean isRealSmsDelivered() {
        return realSmsDelivered;
    }

    public void setRealSmsDelivered(boolean realSmsDelivered) {
        this.realSmsDelivered = realSmsDelivered;
    }
}
