package com.rtech.mandiplus;

import com.rtech.mandiplus.config.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Suite for JWT Token Generation, Parsing, and Cryptographic Security (TC_01 - TC_10)
 */
public class MandiPlusSecurityJwtTest {

    private JwtUtils jwtUtils;
    private final String standardSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        jwtUtils.setJwtSecret(standardSecret);
        jwtUtils.setJwtExpirationMs(86400000);
    }

    @Test
    @DisplayName("TC_01: JWT Token Generation")
    void testTC_01_JwtTokenGeneration() {
        String email = "farmer@mandiplus.com";
        String role = "FARMER";

        String token = jwtUtils.generateJwtToken(email, role);

        assertNotNull(token, "JWT token must not be null");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "Valid JWT must have exactly 3 parts (header.payload.signature)");
        assertTrue(jwtUtils.validateJwtToken(token), "Generated JWT token must be valid");
    }

    @Test
    @DisplayName("TC_02: Extract Username from JWT")
    void testTC_02_ExtractUsernameFromJwt() {
        String email = "farmer@mandiplus.com";
        String role = "FARMER";
        String token = jwtUtils.generateJwtToken(email, role);

        String username = jwtUtils.getUserNameFromJwtToken(token);

        assertEquals("farmer@mandiplus.com", username, "Extracted username must match subject email");
    }

    @Test
    @DisplayName("TC_03: Detect Expired JWT")
    void testTC_03_DetectExpiredJwt() {
        // Generate a token that expired 10 seconds ago
        String expiredToken = jwtUtils.generateJwtToken("farmer@mandiplus.com", "FARMER", -10000L);

        boolean isValid = jwtUtils.validateJwtToken(expiredToken);

        assertFalse(isValid, "validateJwtToken must return false for expired tokens");
    }

    @Test
    @DisplayName("TC_04: Detect Tampered JWT Signature")
    void testTC_04_DetectTamperedJwtSignature() {
        String token = jwtUtils.generateJwtToken("farmer@mandiplus.com", "FARMER");
        String[] parts = token.split("\\.");

        // Tamper with payload by changing base64 data
        String tamperedPayload = parts[1] + "tampered";
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        boolean isValid = jwtUtils.validateJwtToken(tamperedToken);

        assertFalse(isValid, "validateJwtToken must return false for tampered payload claim");
    }

    @Test
    @DisplayName("TC_05: Reject Rogue Secret Key")
    void testTC_05_RejectRogueSecretKey() {
        String rogueSecret = "9999999999999999999999999999999999999999999999999999999999999999";
        String rogueToken = Jwts.builder()
                .setSubject("farmer@mandiplus.com")
                .claim("role", "FARMER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(rogueSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        boolean isValid = jwtUtils.validateJwtToken(rogueToken);

        assertFalse(isValid, "validateJwtToken must reject token signed with foreign/rogue key");
    }

    @Test
    @DisplayName("TC_06: Validate Null JWT Argument")
    void testTC_06_ValidateNullJwtArgument() {
        boolean isValid = jwtUtils.validateJwtToken(null);

        assertFalse(isValid, "validateJwtToken(null) must return false cleanly without NPE");
    }

    @Test
    @DisplayName("TC_07: Validate Blank JWT Argument")
    void testTC_07_ValidateBlankJwtArgument() {
        boolean isValid = jwtUtils.validateJwtToken("   ");

        assertFalse(isValid, "validateJwtToken(blank) must return false cleanly without NPE");
    }

    @Test
    @DisplayName("TC_08: Malformed JSON in JWT")
    void testTC_08_MalformedJsonInJwt() {
        String malformedToken = "hdr.bad_json.sig";

        boolean isValid = jwtUtils.validateJwtToken(malformedToken);

        assertFalse(isValid, "validateJwtToken must reject malformed token");
    }

    @Test
    @DisplayName("TC_09: Truncated JWT Structure")
    void testTC_09_TruncatedJwtStructure() {
        String truncatedToken = "abc.def";

        boolean isValid = jwtUtils.validateJwtToken(truncatedToken);

        assertFalse(isValid, "validateJwtToken must reject truncated 2-part token");
    }

    @Test
    @DisplayName("TC_10: Extract Custom Role Claim")
    void testTC_10_ExtractCustomRoleClaim() {
        String role = "COLD_STORAGE_MANAGER";
        String token = jwtUtils.generateJwtToken("manager@coldstorage.com", role);

        String extractedRole = jwtUtils.getRoleFromJwtToken(token);

        assertEquals("COLD_STORAGE_MANAGER", extractedRole, "Custom role claim must match exactly");
    }
}
