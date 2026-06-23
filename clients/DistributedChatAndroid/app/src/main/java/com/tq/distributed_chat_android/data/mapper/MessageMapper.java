package com.tq.distributed_chat_android.data.mapper;

import android.util.Log;
import androidx.annotation.NonNull;

import com.tq.distributed_chat_android.data.model.AttachmentType;
import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.model.MediaMetadata;

import com.tq.distributed_chat_android.data.protocol.chat.MediaAttachment;
import com.tq.distributed_chat_android.data.protocol.chat.MessageAttachment;
import com.tq.distributed_chat_android.data.protocol.chat.MessagePacket;

import com.tq.distributed_chat_android.data.protocol.type.ContentType;
import com.tq.distributed_chat_android.data.protocol.type.MessageStatus;

import java.lang.reflect.Field;

public class MessageMapper {
    private static final String TAG = "MessageMapper";

    public static ChatMessage toEntity(MessagePacket packet) {
        if (packet == null) return null;

        ChatMessage.ContentType dbContentType = ChatMessage.ContentType.TEXT;
        if (packet.getContentType() != null) {
            try {
                dbContentType = ChatMessage.ContentType.valueOf(packet.getContentType().name());
            } catch (IllegalArgumentException e) {
                dbContentType = ChatMessage.ContentType.TEXT;
            }
        }

        ChatMessage.MessageStatus dbStatus = ChatMessage.MessageStatus.SENT;
        if (packet.getStatus() != null) {
            try {
                dbStatus = ChatMessage.MessageStatus.valueOf(packet.getStatus().name());
            } catch (IllegalArgumentException e) {
                dbStatus = ChatMessage.MessageStatus.SENT;
            }
        }

        long timestamp = packet.getSentTimestamp() > 0 ? packet.getSentTimestamp() : packet.getServerTimestamp();

        ChatMessage entity = new ChatMessage(
                packet.getMessageId(),
                packet.getConversationId(),
                packet.getSenderId(),
                packet.getClientMessageId(),
                timestamp,
                dbContentType,
                packet.getContent(),
                packet.getReplyToMessageId(),
                dbStatus
        );

        MessageAttachment attachment = packet.getAttachment();
        if (attachment != null) {
            if (attachment.getType() != null) {
                try {
                    entity.setAttachmentType(AttachmentType.valueOf(attachment.getType().name()));
                } catch (IllegalArgumentException ignored) {}
            }

            MediaAttachment media = attachment.getMedia();
            if (media != null) {
                entity.setMediaMetadata(mapMediaMetadata(media));
            }
        }

        if (entity.getMediaMetadata() == null && packet.getMediaUrl() != null) {
            if (entity.getAttachmentType() == null) {
                entity.setAttachmentType(AttachmentType.MEDIA);
            }
            MediaMetadata metadata = new MediaMetadata();
            metadata.setDownloadUrl(packet.getMediaUrl());
            metadata.setFileName(packet.getFileName());
            metadata.setFileSize(packet.getFileSize());
            entity.setMediaMetadata(metadata);
        }

        return entity;
    }

    @NonNull
    private static MediaMetadata mapMediaMetadata(MediaAttachment media) {
        MediaMetadata metadata = new MediaMetadata();
        metadata.setMediaId(media.getMediaId());
        metadata.setDownloadUrl(media.getDownloadUrl());
        metadata.setFileName(media.getFileName());
        metadata.setFileSize(media.getFileSize());
        metadata.setMimeType(media.getMimeType());
        metadata.setDurationSeconds(media.getDurationSeconds());
        metadata.setWidth(media.getWidth());
        metadata.setHeight(media.getHeight());
        metadata.setThumbnailUrl(media.getThumbnailUrl());
        return metadata;
    }

    public static MessagePacket toPacket(ChatMessage entity) {
        if (entity == null) return null;

        MessagePacket packet = new MessagePacket();
        packet.setMessageId(entity.getMessageId());
        packet.setConversationId(entity.getConversationId());
        packet.setSenderId(entity.getSenderId());
        packet.setClientMessageId(entity.getClientMessageId());
        packet.setSentTimestamp(entity.getTimestamp());
        packet.setContent(entity.getContent());
        packet.setReplyToMessageId(entity.getReplyToMessageId());

        if (entity.getContentType() != null) {
            try {
                packet.setContentType(ContentType.valueOf(entity.getContentType().name()));
            } catch (IllegalArgumentException ignored) {}
        }

        if (entity.getStatus() != null) {
            try {
                packet.setStatus(MessageStatus.valueOf(entity.getStatus().name()));
            } catch (IllegalArgumentException ignored) {}
        }

        MediaMetadata metadata = entity.getMediaMetadata();

        if (metadata != null) {
            packet.setMediaUrl(metadata.getDownloadUrl());
            packet.setFileName(metadata.getFileName());
            packet.setFileSize(metadata.getFileSize());
        }

        if (entity.getAttachmentType() != null || metadata != null) {
            MessageAttachment attachment = new MessageAttachment();

            if (entity.getAttachmentType() != null) {
                try {
                    com.tq.distributed_chat_android.data.protocol.type.AttachmentType protoType =
                            com.tq.distributed_chat_android.data.protocol.type.AttachmentType.valueOf(entity.getAttachmentType().name());
                    attachment.setType(protoType);
                } catch (IllegalArgumentException ignored) {}
            }

            if (metadata != null) {
                MediaAttachment mediaProto = new MediaAttachment();
                try {
                    setPrivateField(mediaProto, "mediaId", metadata.getMediaId());
                    setPrivateField(mediaProto, "downloadUrl", metadata.getDownloadUrl());
                    setPrivateField(mediaProto, "fileName", metadata.getFileName());
                    setPrivateField(mediaProto, "fileSize", metadata.getFileSize());
                    setPrivateField(mediaProto, "mimeType", metadata.getMimeType());
                    setPrivateField(mediaProto, "durationSeconds", metadata.getDurationSeconds());
                    setPrivateField(mediaProto, "width", metadata.getWidth());
                    setPrivateField(mediaProto, "height", metadata.getHeight());
                    setPrivateField(mediaProto, "thumbnailUrl", metadata.getThumbnailUrl());

                    attachment.setMedia(mediaProto);
                } catch (Exception e) {
                    Log.e(TAG, "Reflection error structural write mismatch to MediaAttachment", e);
                }
            }

            packet.setAttachment(attachment);
        }

        return packet;
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}