package com.tq.distributed_chat_android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TokenExchangeResponse {
    @SerializedName("token")
    private String token;

    public String getToken() { return token; }
}
