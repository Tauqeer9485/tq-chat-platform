package com.chat.server.api.user.dto;

public class UserSearchResponse {
    private String userId;
    private String username;
    private String profilePicture;

    public UserSearchResponse() {
    }

    public UserSearchResponse(String userId, String username, String profilePicture) {
        this.userId = userId;
        this.username = username;
        this.profilePicture = profilePicture;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfileImageUrl() { return profilePicture; }
    public void setProfileImageUrl(String profilePicture) { this.profilePicture = profilePicture; }
}