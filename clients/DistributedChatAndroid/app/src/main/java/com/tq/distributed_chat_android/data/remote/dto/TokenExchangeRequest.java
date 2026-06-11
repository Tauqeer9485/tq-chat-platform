package com.tq.distributed_chat_android.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TokenExchangeRequest {
    @SerializedName("scopes")
    private final List<String> scopes;

    public TokenExchangeRequest(List<String> scopes) {
        this.scopes = scopes;
    }
}
