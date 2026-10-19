package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_challenges", indexes = {
    @Index(name = "idx_otp_phone", columnList = "phone"),
    @Index(name = "idx_otp_created", columnList = "created_at")
})
public class OtpChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    @Column(nullable = false, length = 50)
    private String provider = "FAST2SMS";

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 3;

    @Column(nullable = false)
    private Boolean consumed = false;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private String plaintextOtp;

    @Transient
    private boolean realSmsDelivered = false;

    public OtpChallenge() {
    }

    public OtpChallenge(String phone, String otpHash, String provider, int expirySeconds, int maxAttempts) {
        this.phone = phone;
        this.otpHash = otpHash;
        this.provider = provider != null ? provider : "FAST2SMS";
        this.maxAttempts = maxAttempts > 0 ? maxAttempts : 3;
        this.attempts = 0;
        this.consumed = false;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusSeconds(expirySeconds);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Boolean getConsumed() {
        return consumed;
    }

    public void setConsumed(Boolean consumed) {
        this.consumed = consumed;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPlaintextOtp() {
        return plaintextOtp;
    }

    public void setPlaintextOtp(String plaintextOtp) {
        this.plaintextOtp = plaintextOtp;
    }

    public boolean isRealSmsDelivered() {
        return realSmsDelivered;
    }

    public void setRealSmsDelivered(boolean realSmsDelivered) {
        this.realSmsDelivered = realSmsDelivered;
    }
}
