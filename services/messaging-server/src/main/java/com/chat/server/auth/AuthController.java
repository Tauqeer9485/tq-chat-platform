package com.chat.server.auth;

import com.chat.server.auth.dto.AuthResponse;
import com.chat.server.auth.dto.LoginRequest;
import com.chat.server.auth.dto.SignupRequest;
import com.chat.server.user.User;
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
                    user.getPhone(),
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
                    user.getPhone(),
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
                    user.getPhone(),
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
