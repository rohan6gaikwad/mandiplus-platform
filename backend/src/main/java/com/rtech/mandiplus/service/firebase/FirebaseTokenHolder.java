package com.rtech.mandiplus.service.firebase;

import java.util.Collections;
import java.util.Map;

/**
 * Encapsulates verified identity claims extracted from a valid Firebase ID Token.
 */
public class FirebaseTokenHolder {
    private final String uid;
    private final String phoneNumber;
    private final String email;
    private final String name;
    private final Map<String, Object> claims;

    public FirebaseTokenHolder(String uid, String phoneNumber, String email, String name, Map<String, Object> claims) {
        this.uid = uid;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.name = name;
        this.claims = claims != null ? claims : Collections.emptyMap();
    }

    public String getUid() {
        return uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }
}
