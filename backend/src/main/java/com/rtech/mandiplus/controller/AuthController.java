package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.JwtResponse;
import com.rtech.mandiplus.dto.LoginRequest;
import com.rtech.mandiplus.dto.RegisterRequest;
import com.rtech.mandiplus.dto.ProfileDTO;
import com.rtech.mandiplus.dto.ProfileUpdateRequest;
import com.rtech.mandiplus.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Invalid email or password! " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            String message = authService.registerUser(signUpRequest);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            ProfileDTO profile = authService.getProfile(authentication.getName());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching profile: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody ProfileUpdateRequest request) {
        try {
            authService.updateProfile(authentication.getName(), request);
            return ResponseEntity.ok("Profile updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating profile: " + e.getMessage());
        }
    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody com.rtech.mandiplus.dto.OtpSendRequest request) {
        try {
            com.rtech.mandiplus.dto.OtpResponseDTO response = authService.sendMobileOtp(request.getPhone(), request.getRole());
            return ResponseEntity.ok(response);
        } catch (com.rtech.mandiplus.exception.OtpCooldownException e) {
            return ResponseEntity.status(429).body(java.util.Map.of(
                    "success", false,
                    "error", "COOLDOWN_ACTIVE",
                    "message", e.getMessage(),
                    "remainingSeconds", e.getRemainingSeconds()
            ));
        } catch (com.rtech.mandiplus.exception.OtpRateLimitException e) {
            return ResponseEntity.status(429).body(java.util.Map.of(
                    "success", false,
                    "error", "RATE_LIMIT_EXCEEDED",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "error", "OTP_SEND_FAILED",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody com.rtech.mandiplus.dto.OtpVerifyRequest request) {
        try {
            JwtResponse jwtResponse = authService.verifyOtpAndAuthenticate(request.getPhone(), request.getOtp(), request.getRole());
            return ResponseEntity.ok(jwtResponse);
        } catch (com.rtech.mandiplus.exception.OtpExpiredException e) {
            return ResponseEntity.status(400).body(java.util.Map.of(
                    "success", false,
                    "error", "OTP_EXPIRED",
                    "message", e.getMessage()
            ));
        } catch (com.rtech.mandiplus.exception.OtpAlreadyConsumedException e) {
            return ResponseEntity.status(400).body(java.util.Map.of(
                    "success", false,
                    "error", "OTP_ALREADY_USED",
                    "message", e.getMessage()
            ));
        } catch (com.rtech.mandiplus.exception.OtpMaxAttemptsExceededException e) {
            return ResponseEntity.status(400).body(java.util.Map.of(
                    "success", false,
                    "error", "MAX_ATTEMPTS_EXCEEDED",
                    "message", e.getMessage()
            ));
        } catch (com.rtech.mandiplus.exception.InvalidOtpException e) {
            return ResponseEntity.status(400).body(java.util.Map.of(
                    "success", false,
                    "error", "INVALID_OTP",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "error", "VERIFICATION_FAILED",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/firebase/verify")
    public ResponseEntity<?> authenticateWithFirebase(@Valid @RequestBody com.rtech.mandiplus.dto.FirebaseLoginRequest request) {
        try {
            JwtResponse jwtResponse = authService.verifyFirebaseTokenAndAuthenticate(request.getIdToken(), request.getRole());
            return ResponseEntity.ok(jwtResponse);
        } catch (SecurityException se) {
            return ResponseEntity.status(401).body(java.util.Map.of(
                    "success", false,
                    "error", "FIREBASE_TOKEN_INVALID",
                    "message", se.getMessage()
            ));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "error", "INVALID_REQUEST",
                    "message", iae.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "error", "AUTHENTICATION_FAILED",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping({"/profile/setup-name", "/profile-name", "/setup-profile"})
    public ResponseEntity<?> setupProfileName(Authentication authentication, @RequestBody com.rtech.mandiplus.dto.ProfileNameSetupRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "UNAUTHORIZED", "message", "Authentication required"));
        }
        try {
            if (request == null || request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "NAME_REQUIRED", "message", "Full name is required and cannot be empty or whitespace-only"));
            }
            JwtResponse updated = authService.setupProfileName(authentication.getName(), request.getFullName());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "INVALID_NAME", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "SETUP_FAILED", "message", e.getMessage()));
        }
    }
}
