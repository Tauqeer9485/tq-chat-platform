package com.chat.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider
 * Generates, validates, and extracts claims from JWT tokens
 * Used for REST API authentication and QR-based WebSocket login
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey verificationKey;

    @PostConstruct
    public void init() {
        this.verificationKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate JWT token for authenticated user
     * @param userId UUID of the user
     * @param username username
     * @return JWT token string
     */
    public String generateToken(String userId, String username) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .setIssuedAt(new Date()) // 👈 Uses 'setIssuedAt' in older versions
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // 👈 Uses 'setExpiration'
                .signWith(verificationKey) 
                .compact();
    }

    /**
     * Validate JWT token
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder() // 👈 older versions use parserBuilder()
                    .setSigningKey(verificationKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract userId from JWT token
     * @param token JWT token string
     * @return userId claim
     */
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(verificationKey)
                .build()
                .parseClaimsJws(token)
                .getBody(); // 👈 Uses getBody() instead of getPayload()
        return claims.get("userId", String.class);
    }

    /**
     * Extract username from JWT token
     * @param token JWT token string
     * @return username claim
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(verificationKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("username", String.class);
    }
}
