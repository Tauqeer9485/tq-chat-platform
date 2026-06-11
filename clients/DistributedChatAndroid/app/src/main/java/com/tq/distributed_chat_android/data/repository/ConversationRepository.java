package com.tq.distributed_chat_android.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.tq.distributed_chat_android.data.local.AppDatabase;
import com.tq.distributed_chat_android.data.local.ConversationDao;
import com.tq.distributed_chat_android.data.model.Conversation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ConversationRepository {
    private static volatile ConversationRepository instance;
    private final ConversationDao conversationDao;
    private final ExecutorService executorService;

    private ConversationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.conversationDao = db.conversationDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static ConversationRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (ConversationRepository.class) {
                if (instance == null) {
                    instance = new ConversationRepository(context);
                }
            }
        }
        return instance;
    }

    public LiveData<List<Conversation>> getConversations() {
        return conversationDao.getAllConversations();
    }

    public LiveData<List<Conversation>> getConversationsByType(Conversation.ConversationType type) {
        return conversationDao.getConversationsByType(type.name());
    }

    public void addConversation(Conversation conversation) {
        if (conversation == null) return;
        executorService.execute(() -> {
            conversationDao.insertConversation(conversation);
        });
    }

    public boolean exists(String conversationId) {
        if (conversationId == null) return false;
        Conversation convo = conversationDao.getConversationById(conversationId);
        return convo != null;
    }

    public void clear() {
        executorService.execute(conversationDao::clearAll);
    }
}