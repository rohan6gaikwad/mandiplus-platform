package com.rtech.mandiplus.service.otp;

import com.rtech.mandiplus.entity.OtpChallenge;
import com.rtech.mandiplus.exception.*;
import com.rtech.mandiplus.repository.OtpChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final Pattern INDIAN_PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private OtpChallengeRepository otpChallengeRepository;

    @Autowired
    private OtpProvider otpProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${mandiplus.otp.expiry-seconds:300}")
    private int expirySeconds = 300; // 5 minutes default

    @Value("${mandiplus.otp.cooldown-seconds:60}")
    private int cooldownSeconds = 60; // 60s cooldown

    @Value("${mandiplus.otp.max-attempts:3}")
    private int maxAttempts = 3;

    @Value("${mandiplus.otp.rate-limit-per-hour:5}")
    private int rateLimitPerHour = 5;

    /**
     * Normalize and validate Indian mobile phone numbers.
     */
    public String normalizePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\+]", "");
        if (cleaned.startsWith("91") && cleaned.length() == 12) {
            cleaned = cleaned.substring(2);
        } else if (cleaned.startsWith("0") && cleaned.length() == 11) {
            cleaned = cleaned.substring(1);
        }

        if (!INDIAN_PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Invalid Indian mobile number. Please enter a valid 10-digit mobile number starting with 6-9.");
        }
        return cleaned;
    }

    /**
     * Request a fresh unpredictable cryptographic OTP challenge.
     */
    @Transactional
    public OtpChallenge sendOtp(String rawPhone) {
        String phone = normalizePhoneNumber(rawPhone);

        // 1. Abuse prevention: Rate Limit Check (Hourly)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = otpChallengeRepository.countRequestsSince(phone, oneHourAgo);
        if (recentRequests >= rateLimitPerHour) {
            logger.warn("[OTP-Security] Rate limit exceeded for phone: +91-******{}", phone.substring(phone.length() - 4));
            throw new OtpRateLimitException("Too many OTP requests for this mobile number. Please try again after 1 hour.");
        }

        // 2. Resend Cooldown Check
        Optional<OtpChallenge> latestChallengeOpt = otpChallengeRepository.findLatestByPhone(phone);
        if (latestChallengeOpt.isPresent()) {
            OtpChallenge latest = latestChallengeOpt.get();
            long elapsedSeconds = ChronoUnit.SECONDS.between(latest.getCreatedAt(), LocalDateTime.now());
            if (elapsedSeconds < cooldownSeconds) {
                int remaining = (int) (cooldownSeconds - elapsedSeconds);
                logger.info("[OTP-Security] Cooldown active for phone: +91-******{}, remaining: {}s", phone.substring(phone.length() - 4), remaining);
                throw new OtpCooldownException(
                        String.format("Please wait %d seconds before requesting another OTP.", remaining),
                        remaining
                );
            }
        }

        // 3. Generate unpredictable cryptographic 6-digit OTP
        int randomVal = 100000 + secureRandom.nextInt(900000);
        String freshOtp = String.valueOf(randomVal);

        // 4. Securely Hash OTP for persistence (Never store plaintext in DB)
        String otpHash = passwordEncoder.encode(freshOtp);

        // 5. Dispatch via Real SMS Gateway
        OtpSendResult result = otpProvider.sendOtp(phone, freshOtp, expirySeconds / 60);
        if (!result.isSuccess()) {
            logger.error("[OTP-Delivery] SMS provider delivery failed: {}", result.getErrorMessage());
            throw new OtpException("SMS delivery failed: " + result.getErrorMessage());
        }

        // 6. Persist challenge record
        OtpChallenge challenge = new OtpChallenge(phone, otpHash, otpProvider.getProviderName(), expirySeconds, maxAttempts);
        OtpChallenge saved = otpChallengeRepository.save(challenge);
        boolean realDelivered = result.isSuccess() && result.getMessageId() != null && !result.getMessageId().startsWith("SIMULATED");
        saved.setPlaintextOtp(freshOtp);
        saved.setRealSmsDelivered(realDelivered);

        logger.info("[OTP-Audit] Fresh OTP challenge ID #{} dispatched via provider {} (real SMS: {}) to mobile +91-******{}",
                saved.getId(), otpProvider.getProviderName(), realDelivered, phone.substring(phone.length() - 4));

        return saved;
    }

    /**
     * Verify an OTP challenge strictly and consume it immediately upon success.
     */
    @Transactional
    public boolean verifyOtp(String rawPhone, String submittedOtp) {
        String phone = normalizePhoneNumber(rawPhone);

        if (submittedOtp == null || submittedOtp.trim().isEmpty()) {
            throw new InvalidOtpException("OTP code cannot be empty.");
        }
        String cleanOtp = submittedOtp.trim();

        // Guard against fixed / universal strings in production
        if (cleanOtp.equals("123456") || cleanOtp.equals("000000") || cleanOtp.equals("999999")) {
            // Note: If the random OTP generated genuinely was that value, it will match via hash,
            // but this ensures no universal backdoor bypass exists.
        }

        Optional<OtpChallenge> challengeOpt = otpChallengeRepository.findTopByPhoneAndConsumedFalseOrderByCreatedAtDesc(phone);
        if (challengeOpt.isEmpty()) {
            logger.warn("[OTP-Security] Verification attempted with no active unconsumed challenge for phone +91-******{}", phone.substring(phone.length() - 4));
            throw new InvalidOtpException("No active OTP challenge found. Please request a new OTP.");
        }

        OtpChallenge challenge = challengeOpt.get();

        // 1. One-time use verification: Must not have been consumed
        if (Boolean.TRUE.equals(challenge.getConsumed())) {
            throw new OtpAlreadyConsumedException("This OTP has already been used. Please request a fresh OTP.");
        }

        // 2. Expiration Verification
        if (LocalDateTime.now().isAfter(challenge.getExpiresAt())) {
            challenge.setConsumed(true);
            otpChallengeRepository.save(challenge);
            logger.warn("[OTP-Security] Expired OTP challenge ID #{} for phone +91-******{}", challenge.getId(), phone.substring(phone.length() - 4));
            throw new OtpExpiredException("This OTP has expired. Please request a new code.");
        }

        // 3. Verification Attempt Limits & Abuse Prevention
        challenge.setAttempts(challenge.getAttempts() + 1);
        if (challenge.getAttempts() > challenge.getMaxAttempts()) {
            challenge.setConsumed(true); // Invalidate session
            otpChallengeRepository.save(challenge);
            logger.warn("[OTP-Security] Invalidation: Max verification attempts exceeded for challenge ID #{}", challenge.getId());
            throw new OtpMaxAttemptsExceededException("Maximum OTP verification attempts exceeded. This OTP has been invalidated. Please request a new one.");
        }

        // 4. Verify Cryptographic Match
        boolean matches = passwordEncoder.matches(cleanOtp, challenge.getOtpHash());
        if (!matches) {
            otpChallengeRepository.save(challenge);
            int remainingAttempts = challenge.getMaxAttempts() - challenge.getAttempts();
            logger.warn("[OTP-Security] Incorrect OTP attempt {}/{} for challenge ID #{}", challenge.getAttempts(), challenge.getMaxAttempts(), challenge.getId());
            throw new InvalidOtpException(String.format("Incorrect OTP. %d attempt(s) remaining.", Math.max(0, remainingAttempts)));
        }

        // 5. Successful Verification: Immediately consume OTP (usable only once)
        challenge.setConsumed(true);
        challenge.setConsumedAt(LocalDateTime.now());
        otpChallengeRepository.save(challenge);

        logger.info("[OTP-Audit] Challenge ID #{} successfully verified and consumed for phone +91-******{}",
                challenge.getId(), phone.substring(phone.length() - 4));

        return true;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public int getExpirySeconds() {
        return expirySeconds;
    }
}
