package com.tq.distributed_chat_android.data.model;

public class SearchUser {
    private final String userId;
    private final String username;
    private final String profilePicture;
    public SearchUser(String userId, String username, String profilePicture) {
        this.userId = userId;
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getProfilePicture() { return profilePicture; }
}