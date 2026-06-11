package com.tq.distributed_chat_android.lifecycle;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.tq.distributed_chat_android.data.remote.ChatWebSocketManager;
import com.tq.distributed_chat_android.util.SessionManager;

public class AppLifecycleObserver implements DefaultLifecycleObserver {
    private final Context context;

    public AppLifecycleObserver(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        SessionManager sessionManager = new SessionManager(context);

        String token = sessionManager.getAuthToken();

        if (token != null) {
            ChatWebSocketManager.getInstance(context.getApplicationContext(), null)
                    .connectWithAuthentication(token);
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        ChatWebSocketManager.getInstance(context.getApplicationContext(), null)
                .disconnect();
    }
}