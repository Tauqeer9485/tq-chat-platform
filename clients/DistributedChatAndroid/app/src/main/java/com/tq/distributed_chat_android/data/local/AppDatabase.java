package com.tq.distributed_chat_android.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.model.Conversation;

@Database(entities = {ChatMessage.class, Conversation.class}, version = 2, exportSchema = false)
@TypeConverters({AppTypeConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract MessageDao messageDao();
    public abstract ConversationDao conversationDao();


    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "distributed_chat_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}