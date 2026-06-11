package com.tq.distributed_chat_android.lifecycle;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

public class ChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get()
                .getLifecycle()
                .addObserver(new AppLifecycleObserver(this));
    }
}