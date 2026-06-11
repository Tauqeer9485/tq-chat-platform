package com.tq.distributed_chat_android.data.mapper;

import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.protocol.chat.MessagePacket;

public class MessageMapper {
    public static ChatMessage mapToDomain(MessagePacket packet) {
        if (packet == null) return null;

        ChatMessage.ContentType modelContentType = ChatMessage.ContentType.TEXT;
        if (packet.getContentType() != null) {
            switch (packet.getContentType()) {
                case IMAGE: modelContentType = ChatMessage.ContentType.IMAGE; break;
                case AUDIO: modelContentType = ChatMessage.ContentType.AUDIO; break;
                case VIDEO: modelContentType = ChatMessage.ContentType.VIDEO; break;
                case FILE:  modelContentType = ChatMessage.ContentType.FILE; break;
                default:    modelContentType = ChatMessage.ContentType.TEXT; break;
            }
        }

        ChatMessage.MessageStatus modelStatus = ChatMessage.MessageStatus.PENDING;
        if (packet.getStatus() != null) {
            switch (packet.getStatus()) {
                case SENT:      modelStatus = ChatMessage.MessageStatus.SENT; break;
                case DELIVERED: modelStatus = ChatMessage.MessageStatus.DELIVERED; break;
                case READ:      modelStatus = ChatMessage.MessageStatus.READ; break;
                case FAILED:    modelStatus = ChatMessage.MessageStatus.FAILED; break;
                default:        modelStatus = ChatMessage.MessageStatus.PENDING; break;
            }
        }

        long timestamp = packet.getServerTimestamp() != 0 ? packet.getServerTimestamp() : packet.getSentTimestamp();

        return new ChatMessage(
                packet.getMessageId(),
                packet.getConversationId(),
                packet.getSenderId(),
                packet.getClientMessageId(),
                timestamp,
                modelContentType,
                packet.getContent(),
                packet.getReplyToMessageId(),
                packet.getMediaUrl(),
                packet.getFileName(),
                packet.getFileSize(),
                modelStatus
        );
    }
}