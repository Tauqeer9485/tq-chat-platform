package com.tq.distributed_chat_android.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "TQChatSession";

    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private static String currentUserId;
    private static String currentUsername;
    private static String currentEmail;

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );

        editor = sharedPreferences.edit();

        // Restore cached values after app restart
        currentUserId = sharedPreferences.getString(KEY_USER_ID, null);
        currentUsername = sharedPreferences.getString(KEY_USERNAME, null);
        currentEmail = sharedPreferences.getString(KEY_EMAIL, null);
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public void saveUser(
            String userId,
            String username,
            String email
    ) {
        currentUserId = userId;
        currentUsername = username;
        currentEmail = email;

        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public static String getUserId() {
        return currentUserId;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static String getEmail() {
        return currentEmail;
    }

    public void clearSession() {

        currentUserId = null;
        currentUsername = null;
        currentEmail = null;

        editor.clear();
        editor.apply();
    }
}