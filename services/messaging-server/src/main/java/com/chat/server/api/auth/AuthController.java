package com.chat.server.api.auth;

import com.chat.server.api.auth.dto.AuthResponse;
import com.chat.server.api.auth.dto.LoginRequest;
import com.chat.server.api.auth.dto.SignupRequest;
import com.chat.server.api.auth.dto.TokenExchangeRequest;
import com.chat.server.core.user.User;
import com.chat.server.core.user.UserService;
import com.chat.server.security.JwtTokenProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller
 * Handles login, signup, and token verification endpoints
 * Base URL: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;
    
    @Autowired
    private JwtTokenProvider tokenProvider;


    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/auth/login
     * Login with username and password
     * 
     * Request body:
     * {
     *   "username": "Tauqeer",
     *   "password": "password123"
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "token": "eyJhbGc...",
     *   "user": {
     *     "id": 1,
     *     "username": "Tauqeer",
     *     "userId": "550e8400-e29b-41d4-a716-446655440001"
     *   }
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticateUser(request.getUsername(), request.getPassword());
            String token = userService.generateToken(user);

            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getUserId(),
                    user.getProfilePicture()
            );

            AuthResponse response = new AuthResponse(
                    true,
                    "Login successful",
                    token,
                    userInfo
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Login failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Server error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/signup
     * Register new user
     * 
     * Request body:
     * {
     *   "username": "Alice",
     *   "email": "alice@example.com",
     *   "password": "password123",
     *   "confirmPassword": "password123"
     * }
     * 
     * Response: Same as login if successful
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        try {
            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Username is required"));
            }

            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Email is required"));
            }

            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Password is required"));
            }

            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponse(false, "Passwords do not match"));
            }

            User user = userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
            String token = userService.generateToken(user);

            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getUserId(),
                    user.getProfilePicture()
            );

            AuthResponse response = new AuthResponse(
                    true,
                    "Signup successful",
                    token,
                    userInfo
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(false, "Signup failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Server error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/verify
     * Verify JWT token and get user info
     * 
     * Request body:
     * {
     *   "token": "eyJhbGc..."
     * }
     * 
     * Response: User info if token valid
     */
    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verifyToken(@RequestBody TokenRequest request) {
        try {
            var userOptional = userService.verifyToken(request.getToken());

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(false, "Token is invalid or expired"));
            }

            User user = userOptional.get();

            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getUserId(),
                    user.getProfilePicture()
            );

            AuthResponse response = new AuthResponse(true, "Token valid", request.getToken(), userInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Server error: " + e.getMessage()));
        }
    }

    
    /**
     * GET /api/auth/.well-known/jwks.json
     * Return JWKS metadata for public keys used to sign JWTs. This is used by clients to verify token signatures.
     * Response:
     * {
     *   "keys": [
     *     {
     *       "kty": "RSA",
     *       "kid": "key-id",
     *       "use": "sig",
     *       "alg": "RS256",
     *       "n": "base64url-modulus",
     *       "e": "base64url-exponent"
     *     }
     *   ]
     * }
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        try {
            Map<String, Object> jwksKeyJson = tokenProvider.getPublicJwksDetails();
            
            Map<String, Object> response = new HashMap<>();
            response.put("keys", Collections.singletonList(jwksKeyJson));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/auth/tokens/exchange
     * Exchange a low-privilege long-lived session token for an ultra-short-lived, tightly scoped token.
     * * Request Header: 
     * Authorization: Bearer <Standard_Long_Lived_Token>
     * * Request Body:
     * { 
     *   "scopes": ["messaging:connect"] 
     * } 
     * or 
     * {
     *   "scopes": ["media:write", "media:read"] 
     * }
     */
    @PostMapping("tokens/exchange")
    public ResponseEntity<?> exchangeToken(@RequestHeader("Authorization") String authHeader, @RequestBody TokenExchangeRequest tokenRequest) {
        try {
            String sessionToken = JwtTokenProvider.extractTokenFromAuthHeader(authHeader);
            if (sessionToken == null || !tokenProvider.validateToken(sessionToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid or expired session credentials"));
            }

            if (tokenRequest.getScopes() == null || tokenRequest.getScopes().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Missing required field: scopes"));
            }

            String userId = tokenProvider.getUserIdFromToken(sessionToken);
            String username = tokenProvider.getUsernameFromToken(sessionToken);

            long tokenLifespanMs = determineLifespan(tokenRequest.getScopes());

            String shortLivedToken = tokenProvider.generateShortLivedToken(userId, username, tokenRequest.getScopes(), tokenLifespanMs);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Short-lived scoped token issued successfully");
            response.put("token", shortLivedToken);
            response.put("expiresIn", tokenLifespanMs);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Server error processing token exchange: " + e.getMessage()));
        }
    }

    /**
     * Determine token lifespan based on requested scopes.
     */
    private long determineLifespan(List<String> scopes) {
        if (scopes.contains("messaging:connect")) {
            return 60000L; 
        }
        
        return 900000L;
    }
    
    /**
     * Token Request DTO for verify endpoint
     */
    public static class TokenRequest {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
