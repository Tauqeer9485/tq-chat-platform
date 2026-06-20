package com.tq.distributed_chat_android.util;

public class ActiveConversationManager {

    private static final ActiveConversationManager INSTANCE =
            new ActiveConversationManager();

    private volatile String activeConversationId;

    private ActiveConversationManager() {
    }

    public static ActiveConversationManager getInstance() {
        return INSTANCE;
    }

    public void setActiveConversation(String conversationId) {
        this.activeConversationId = conversationId;
    }

    public void clearActiveConversation() {
        this.activeConversationId = null;
    }

    public String getActiveConversationId() {
        return activeConversationId;
    }

    public boolean isActive(String conversationId) {
        return conversationId != null
                && conversationId.equals(activeConversationId);
    }
}