package com.rtech.mandiplus.dto;

import jakarta.validation.constraints.NotBlank;

public class FirebaseLoginRequest {

    @NotBlank(message = "Firebase ID token is required")
    private String idToken;

    private String role; // Optional requested role for new user registration (FARMER, BUYER, etc.)

    public FirebaseLoginRequest() {
    }

    public FirebaseLoginRequest(String idToken, String role) {
        this.idToken = idToken;
        this.role = role;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public void setFirebaseIdToken(String firebaseIdToken) {
        this.idToken = firebaseIdToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
