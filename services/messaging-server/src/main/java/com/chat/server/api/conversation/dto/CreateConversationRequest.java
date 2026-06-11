package com.chat.server.api.conversation.dto;

/**
 * Create Conversation Request DTO
 * Payload for creating a new conversation (e.g. DM)
 */
public class CreateConversationRequest {

    private String targetUserId;

    public CreateConversationRequest() {}
    
    public CreateConversationRequest(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
}