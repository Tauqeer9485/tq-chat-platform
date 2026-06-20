package com.tq.distributed_chat_android.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tq.distributed_chat_android.data.model.ChatMessage;

import java.util.List;

@Dao
public interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(ChatMessage message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(List<ChatMessage> messages);

    @Update
    void updateMessage(ChatMessage message);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    LiveData<List<ChatMessage>> getMessagesForConversation(String conversationId);

    @Query("SELECT * FROM messages WHERE messageId = :messageId LIMIT 1")
    ChatMessage getMessageById(long messageId);

    @Query("SELECT * FROM messages WHERE clientMessageId = :clientMessageId LIMIT 1")
    ChatMessage getMessageByClientMId(String clientMessageId);

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    void deleteConversationHistory(String conversationId);
}