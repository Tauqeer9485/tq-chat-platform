package com.tq.distributed_chat_android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class VerifyResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private AuthResponse.UserDetails user;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public AuthResponse.UserDetails getUser() { return user; }
}