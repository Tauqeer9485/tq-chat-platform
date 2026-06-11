package com.tq.distributed_chat_android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TokenVerifyRequest {
    @SerializedName("token")
    private final String token;

    public TokenVerifyRequest(String token) {
        this.token = token;
    }
}
