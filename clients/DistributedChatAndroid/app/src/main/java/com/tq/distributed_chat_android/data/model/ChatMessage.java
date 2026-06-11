package com.tq.distributed_chat_android.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "messages")
public class ChatMessage {
    public enum ContentType {
        TEXT, IMAGE, AUDIO, VIDEO, FILE
    }

    public enum MessageStatus {
        PENDING, SENT, DELIVERED, READ, FAILED
    }

    @PrimaryKey
    private final long messageId;
    private final String conversationId;
    private final String senderId;
    private final String clientMessageId;
    private final long timestamp;
    private final ContentType contentType;
    private final String content;

    private final String replyToMessageId;
    private final String mediaUrl;
    private final String fileName;
    private final long fileSize;

    private MessageStatus status;

    public ChatMessage(
            long messageId, String conversationId, String senderId, String clientMessageId,
            long timestamp, ContentType contentType, String content, String replyToMessageId,
            String mediaUrl, String fileName, long fileSize, MessageStatus status
    ) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.clientMessageId = clientMessageId;
        this.timestamp = timestamp;
        this.contentType = contentType;
        this.content = content;
        this.replyToMessageId = replyToMessageId;
        this.mediaUrl = mediaUrl;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.status = status;
    }

    @Ignore
    public ChatMessage(long messageId, String conversationId, String senderId, String content, ContentType contentType) {
        this(
                messageId,
                conversationId,
                senderId,
                "client_msg_" + System.currentTimeMillis(),
                System.currentTimeMillis(),
                contentType,
                content,
                null, null, null, 0,
                MessageStatus.PENDING
        );
    }

    public long getMessageId() { return messageId; }
    public String getConversationId() { return conversationId; }
    public String getSenderId() { return senderId; }
    public String getClientMessageId() { return clientMessageId; }
    public long getTimestamp() { return timestamp; }
    public ContentType getContentType() { return contentType; }
    public String getContent() { return content; }
    public String getReplyToMessageId() { return replyToMessageId; }
    public String getMediaUrl() { return mediaUrl; }
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public MessageStatus getStatus() { return status; }

    public void setStatus(MessageStatus status) { this.status = status; }
}