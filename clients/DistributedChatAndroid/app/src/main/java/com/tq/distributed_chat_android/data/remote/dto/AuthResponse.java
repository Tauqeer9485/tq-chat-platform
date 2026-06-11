package com.tq.distributed_chat_android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("token")
    private String token;
    @SerializedName("user")
    private UserDetails user;
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public UserDetails getUser() {
        return user;
    }
    public void setUser(UserDetails user) {
        this.user = user;
    }

    public static class UserDetails {
        @SerializedName("id")
        private long id;
        @SerializedName("username")
        private String username;
        @SerializedName("email")
        private String email;
        @SerializedName("userId")
        private String userId;
        @SerializedName("profilePicture")
        private String profilePicture;

        public long getId() {
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
        public String getProfilePicture() {
            return profilePicture;
        }
    }
}