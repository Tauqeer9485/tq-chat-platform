package com.chat.server.conversation.dto;

/**
 * Create Conversation Request DTO
 * Payload for creating a new conversation (e.g. DM)
 */
public class CreateConversationRequest {

    private String targetUserId;

    public CreateConversationRequest() {}

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
}