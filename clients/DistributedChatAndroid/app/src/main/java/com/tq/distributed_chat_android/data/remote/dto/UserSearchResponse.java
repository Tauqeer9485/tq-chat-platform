package com.tq.distributed_chat_android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UserSearchResponse {
    @SerializedName("userId")
    private String userId;
    @SerializedName("username")
    private String username;
    @SerializedName("profilePicture")
    private String profilePicture;

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getProfilePicture() { return profilePicture; }
}