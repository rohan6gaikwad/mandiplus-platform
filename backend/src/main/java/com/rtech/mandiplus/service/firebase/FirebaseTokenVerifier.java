package com.rtech.mandiplus.service.firebase;

public interface FirebaseTokenVerifier {
    /**
     * Verifies the authenticity, issuer, audience, and expiry of a Firebase ID token.
     * Extracts verified identity claims (UID, phone number).
     *
     * @param idToken raw Firebase ID token from client
     * @return FirebaseTokenHolder containing verified claims
     */
    FirebaseTokenHolder verifyIdToken(String idToken);
}
