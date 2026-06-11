package com.tq.distributed_chat_android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class DirectChatRequest {
    @SerializedName("targetUserId")
    private final String targetUserId;

    public DirectChatRequest(String targetUserId) {
        this.targetUserId = targetUserId;
    }
}