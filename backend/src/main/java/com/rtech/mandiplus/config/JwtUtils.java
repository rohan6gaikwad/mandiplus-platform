package com.rtech.mandiplus.config;

import com.rtech.mandiplus.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${mandiplus.jwt.secret:${JWT_SECRET:}}")
    private String jwtSecret;

    @Value("${mandiplus.jwt.expirationMs:86400000}")
    private int jwtExpirationMs;

    @jakarta.annotation.PostConstruct
    public void validateConfiguration() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            logger.warn("[Mandi+ Security] Missing required configuration: 'mandiplus.jwt.secret' (or 'JWT_SECRET'). JWT signing will require this variable in your .env or environment.");
        } else if (jwtSecret.trim().length() < 32) {
            logger.warn("[Mandi+ Security] Configuration advisory: 'mandiplus.jwt.secret' should be at least 32 characters (256 bits) for cryptographic security.");
        }
    }

    private Key getSigningKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("[Mandi+ Security] Missing required configuration: 'mandiplus.jwt.secret' (or 'JWT_SECRET'). Please set this variable in your .env or environment.");
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public void setJwtExpirationMs(int jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateJwtToken(String email, String role) {
        return generateJwtToken(email, role, this.jwtExpirationMs);
    }

    public String generateJwtToken(String email, String role, long expirationMs) {
        var builder = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date());

        if (role != null) {
            builder.claim("role", role);
        }

        builder.setExpiration(new Date(System.currentTimeMillis() + expirationMs));
        return builder.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getRoleFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    public Claims getClaimsFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateJwtToken(String authToken) {
        if (authToken == null || authToken.trim().isEmpty()) {
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT parsing error: {}", e.getMessage());
        }
        return false;
    }
}
