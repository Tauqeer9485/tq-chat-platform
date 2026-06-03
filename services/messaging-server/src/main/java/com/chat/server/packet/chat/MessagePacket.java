package com.chat.server.packet.chat;

import com.chat.server.packet.base.BasePacket;
import com.chat.server.packet.type.ContentType;
import com.chat.server.packet.type.MessageStatus;
import com.chat.server.packet.type.PacketType;
import com.chat.server.session.UserId;

public class MessagePacket extends BasePacket {

    private long messageId;
    private String conversationId;
    private UserId senderId;
    private String clientMessageId;
    private long sentTimestamp;
    private long serverTimestamp;
    private ContentType contentType;
    private String content;
    private String replyToMessageId;
    private String mediaUrl;
    private String fileName;
    private long fileSize;
    private MessageStatus status;

    public MessagePacket() {
        super(PacketType.CHAT_MESSAGE);
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public UserId getSenderId() {
        return senderId;
    }

    public void setSenderId(UserId senderId) {
        this.senderId = senderId;
    }

    public String getClientMessageId() {
        return clientMessageId;
    }

    public void setClientMessageId(String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }

    public long getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(long sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public long getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}