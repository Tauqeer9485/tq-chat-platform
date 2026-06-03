package com.chat.server.auth.dto;

/**
 * Auth Response DTO
 * Response payload for login/signup endpoints
 */
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private UserInfo user;

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, String token, UserInfo user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /**
     * User Info nested DTO
     */
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String userId;
        private String phone;
        private String profilePicture;

        public UserInfo(Long id, String username, String email, String userId, String phone, String profilePicture) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.userId = userId;
            this.phone = phone;
            this.profilePicture = profilePicture;
        }

        // Getters
        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getUserId() {
            return userId;
        }

        public String getPhone() {
            return phone;
        }

        public String getProfilePicture() {
            return profilePicture;
        }
    }
}
