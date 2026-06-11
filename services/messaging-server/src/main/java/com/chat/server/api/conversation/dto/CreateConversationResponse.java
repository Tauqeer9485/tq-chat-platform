package com.chat.server.api.conversation.dto;

/**
 * Create Conversation Response DTO
 * Response payload for create conversation endpoint
 */
public class CreateConversationResponse {

    private String conversationId;

    public CreateConversationResponse() {}

    public CreateConversationResponse(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}