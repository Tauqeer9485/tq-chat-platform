package com.tq.distributed_chat_android.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tq.distributed_chat_android.data.model.Conversation;

import java.util.List;

@Dao
public interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConversation(Conversation conversation);

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    LiveData<List<Conversation>> getAllConversations();

    // Utility: Fetch only group chats or only direct messages for split-tab UI designs
    @Query("SELECT * FROM conversations WHERE conversationType = :type ORDER BY timestamp DESC")
    LiveData<List<Conversation>> getConversationsByType(String type);

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId LIMIT 1")
    Conversation getConversationById(String conversationId);

    @Query("DELETE FROM conversations")
    void clearAll();
}