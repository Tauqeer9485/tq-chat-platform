package com.tq.distributed_chat_android.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.tq.distributed_chat_android.data.local.AppDatabase;
import com.tq.distributed_chat_android.data.local.MessageDao;
import com.tq.distributed_chat_android.data.model.ChatMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageRepository {
    private static volatile MessageRepository instance;
    private final MessageDao messageDao;
    private final ExecutorService executorService;

    private MessageRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.messageDao = database.messageDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static MessageRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (MessageRepository.class) {
                if (instance == null) {
                    instance = new MessageRepository(context);
                }
            }
        }
        return instance;
    }

    public void insertMessage(ChatMessage message) {
        if (message == null) return;

        executorService.execute(() -> {
            messageDao.insertMessage(message);
        });
    }

    public void insertMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return;

        executorService.execute(() -> {
            messageDao.insertMessages(messages);
        });
    }

    public void updateMessage(ChatMessage message) {
        if (message == null) return;

        executorService.execute(() -> {
            messageDao.updateMessage(message);
        });
    }

    public void updateUploadedMediaUrl(String clientMessageId, String localOrRemotePath, ChatMessage.MessageStatus newStatus) {
        executorService.execute(() -> {
            ChatMessage message = messageDao.getMessageByClientMId(clientMessageId);
            if (message != null) {
                message.setMediaUrl(localOrRemotePath);
                message.setStatus(newStatus);
                messageDao.updateMessage(message);
            }
        });
    }

    public LiveData<List<ChatMessage>> getMessagesForConversation(String conversationId) {
        return messageDao.getMessagesForConversation(conversationId);
    }

    public void deleteConversationHistory(String conversationId) {
        executorService.execute(() -> {
            messageDao.deleteConversationHistory(conversationId);
        });
    }
}