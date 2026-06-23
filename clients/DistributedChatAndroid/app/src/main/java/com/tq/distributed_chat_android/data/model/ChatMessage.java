package com.tq.distributed_chat_android.data.model;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "messages")
public class ChatMessage {
    public enum ContentType {
        TEXT, IMAGE, AUDIO, VIDEO, FILE
    }

    public enum MessageStatus {
        PENDING, SENT, DELIVERED, READ, FAILED
    }

    @PrimaryKey
    @SerializedName("messageId")
    private final long messageId;
    @SerializedName("conversationId")
    private final String conversationId;
    @SerializedName("senderId")
    private final String senderId;
    @SerializedName("clientMessageId")
    private final String clientMessageId;
    @SerializedName("timestamp")
    private final long timestamp;
    @SerializedName("contentType")
    private final ContentType contentType;
    @SerializedName("content")
    private final String content;
    @SerializedName("replyToMessageId")
    private final String replyToMessageId;
    @SerializedName("status")
    private MessageStatus status;
    @Embedded(prefix = "media_")
    private MediaMetadata mediaMetadata;
    private AttachmentType attachmentType;

    public ChatMessage(
            long messageId, String conversationId, String senderId, String clientMessageId,
            long timestamp, ContentType contentType, String content, String replyToMessageId,
            MessageStatus status
    ) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.clientMessageId = clientMessageId;
        this.timestamp = timestamp;
        this.contentType = contentType;
        this.content = content;
        this.replyToMessageId = replyToMessageId;
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
                null,
                MessageStatus.PENDING
        );
    }

    @Ignore
    public ChatMessage(
            long messageId, String conversationId, String senderId, String clientMessageId,
            long timestamp, ContentType contentType, String content, String replyToMessageId,
            MessageStatus status, AttachmentType attachmentType, MediaMetadata mediaMetadata
    ) {
        this(messageId, conversationId, senderId, clientMessageId, timestamp, contentType, content, replyToMessageId, status);
        this.attachmentType = attachmentType;
        this.mediaMetadata = mediaMetadata;
    }

    public long getMessageId() { return messageId; }
    public String getConversationId() { return conversationId; }
    public String getSenderId() { return senderId; }
    public String getClientMessageId() { return clientMessageId; }
    public long getTimestamp() { return timestamp; }
    public ContentType getContentType() { return contentType; }
    public String getContent() { return content; }
    public String getReplyToMessageId() { return replyToMessageId; }

    public void setStatus(MessageStatus status) { this.status = status; }
    public MessageStatus getStatus() {
        return status;
    }

    public MediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public void setMediaMetadata(MediaMetadata mediaMetadata) {
        this.mediaMetadata = mediaMetadata;
    }
}