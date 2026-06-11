package com.tq.distributed_chat_android.data.local;

import androidx.room.TypeConverter;
import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.model.Conversation;

public class AppTypeConverters {
    @TypeConverter
    public static String fromContentType(ChatMessage.ContentType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static ChatMessage.ContentType toContentType(String type) {
        return type == null ? null : ChatMessage.ContentType.valueOf(type);
    }

    @TypeConverter
    public static String fromMessageStatus(ChatMessage.MessageStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static ChatMessage.MessageStatus toMessageStatus(String status) {
        return status == null ? null : ChatMessage.MessageStatus.valueOf(status);
    }

    @TypeConverter
    public static String fromConversationType(Conversation.ConversationType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static Conversation.ConversationType toConversationType(String type) {
        return type == null ? null : Conversation.ConversationType.valueOf(type);
    }
}