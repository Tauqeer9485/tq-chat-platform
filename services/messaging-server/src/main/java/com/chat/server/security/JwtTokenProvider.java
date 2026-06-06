package com.chat.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider (Asymmetric RS256 Implementation)
 * Generates tokens using a Private Key, and exposes Public Key parameters via JWKS.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final String KEY_ID = "chat-platform-key-id-v1";

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    /**
     * Initialize the RSA key pair on startup.
     */
    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            
            this.publicKey = (RSAPublicKey) kp.getPublic();
            this.privateKey = (RSAPrivateKey) kp.getPrivate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RSA cryptographic keys", e);
        }
    }

    /**
     * Internal method to build a JWT token with specified claims and scopes, signed with the RSA Private Key.
     */
    private String buildToken(String userId, String username, long expirationMs, List<String> scopes) {
        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID) 
                .claim("userId", userId)
                .claim("username", username)
                .claim("scopes", scopes)
                .setAudience("media-service")  
                .setIssuer("tq-chat-platform")
                .setIssuedAt(new Date()) 
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) 
                .signWith(privateKey, SignatureAlgorithm.RS256) 
                .compact();
    }

    /**
     * Standard token generation method for REST API authentication.
     */
    public String generateToken(String userId, String username) {
        List<String> baselineScopes = List.of("user:profile");
        return buildToken(userId, username, this.jwtExpiration, baselineScopes);
    }

    /**
     * token generation method for short-lived tokens.
     */
    public String generateShortLivedToken(String userId, String username, List<String> requestedScopes, long expiryMs) {
        List<String> allowedScopes = List.of("media:read", "media:write", "media:delete", "websocket:connect");
        
        List<String> sanitizedScopes = requestedScopes.stream()
                .filter(allowedScopes::contains)
                .toList();

        if (sanitizedScopes.isEmpty()) {
            throw new IllegalArgumentException("Access Denied: No valid system permissions requested");
        }

        return buildToken(userId, username, expiryMs, sanitizedScopes);
    }

    /**
     * Validate JWT token using the RSA Public Key
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder() 
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract userId from JWT token using the RSA Public Key
     */
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody(); 
        return claims.get("userId", String.class);
    }

    /**
     * Extract username from JWT token using the RSA Public Key
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("username", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getScopesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("scopes", List.class);
    }

    /**
     * Extracts the mathematical properties (modulus and exponent) of the RSA public key
     * and formats them into a standard JWK map representation.
     */
    public Map<String, Object> getPublicJwksDetails() {
        Map<String, Object> jwk = new HashMap<>();
        
        // Base64URL encode the standard components (omitting any trailing equals sign padding)
        String modulus = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray());
        String exponent = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray());

        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("alg", "RS256");
        jwk.put("kid", KEY_ID);
        jwk.put("n", modulus);
        jwk.put("e", exponent);

        return jwk;
    }
}
