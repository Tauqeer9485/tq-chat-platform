package com.tq.distributed_chat_android.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "conversations")
public class Conversation {
    public enum ConversationType {
        DIRECT,
        GROUP
    }

    @PrimaryKey
    @NonNull
    private final String conversationId;
    private final String participantName;
    private final String lastMessage;
    private final long timestamp;
    private final ConversationType conversationType;

    public Conversation(@NonNull String conversationId, String participantName,
                        String lastMessage, long timestamp, ConversationType conversationType) {
        this.conversationId = conversationId;
        this.participantName = participantName;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.conversationType = conversationType;
    }

    @NonNull
    public String getConversationId() { return conversationId; }
    public String getParticipantName() { return participantName; }
    public String getLastMessage() { return lastMessage; }
    public long getTimestamp() { return timestamp; }
    public ConversationType getConversationType() { return conversationType; }

    public boolean isGroupChat() {
        return this.conversationType == ConversationType.GROUP;
    }
}