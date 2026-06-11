package com.tq.distributed_chat_android.data.remote;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.tq.distributed_chat_android.BuildConfig;
import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.protocol.chat.MessagePacket;
import com.tq.distributed_chat_android.data.protocol.dispatcher.PacketDispatcher;
import com.tq.distributed_chat_android.data.protocol.handler.MessagePacketHandler; // Added import
import com.tq.distributed_chat_android.data.protocol.type.PacketType;            // Added import
import com.tq.distributed_chat_android.data.remote.dto.TokenExchangeRequest;
import com.tq.distributed_chat_android.data.remote.dto.TokenExchangeResponse;

import java.util.Collections;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatWebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final String WS_URL = BuildConfig.CHATS_WS_URL;
    private static volatile ChatWebSocketManager instance;
    private final OkHttpClient client;
    private WebSocket webSocket;
    private boolean isConnecting = false;
    private boolean isExchangingToken = false;
    private final Gson gson = new Gson();

    public interface WebSocketMessageListener {
        void onConnectionStateChanged(boolean isConnected);
    }

    private WebSocketMessageListener messageListener;
    private final PacketDispatcher packetDispatcher;

    private ChatWebSocketManager(Context context, OkHttpClient sharedClient) {
        this.client = sharedClient;
        this.packetDispatcher = new PacketDispatcher();
        this.packetDispatcher.registerHandler(
                PacketType.CHAT_MESSAGE,
                MessagePacket.class,
                new MessagePacketHandler(context.getApplicationContext())
        );
    }

    public static ChatWebSocketManager getInstance(Context context, OkHttpClient sharedClient) {
        if (instance == null) {
            synchronized (ChatWebSocketManager.class) {
                if (instance == null) {
                    if (sharedClient == null) {
                        sharedClient = new OkHttpClient.Builder()
                                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                                .build();
                    }
                    instance = new ChatWebSocketManager(context, sharedClient);
                }
            }
        }
        return instance;
    }

    public synchronized void connectWithAuthentication(final String longLivedToken) {
        if (webSocket != null || isConnecting || isExchangingToken) {
            Log.d(TAG, "Connection request ignored: Pipeline is busy or already active.");
            return;
        }

        isExchangingToken = true;
        String authHeader = "Bearer " + longLivedToken;
        TokenExchangeRequest requestPayload = new TokenExchangeRequest(Collections.singletonList("websocket:connect"));

        ApiClient.getAuthService().exchangeToken(authHeader, requestPayload)
                .enqueue(new Callback<TokenExchangeResponse>() {
                    @Override
                    public void onResponse(Call<TokenExchangeResponse> call, retrofit2.Response<TokenExchangeResponse> response) {
                        synchronized (ChatWebSocketManager.this) {
                            isExchangingToken = false;
                            if (response.isSuccessful() && response.body() != null) {
                                establishWebSocketConnection(response.body().getToken());
                            } else {
                                Log.e(TAG, "Token exchange rejected by security cluster. Status Code: " + response.code());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TokenExchangeResponse> call, Throwable t) {
                        synchronized (ChatWebSocketManager.this) {
                            isExchangingToken = false;
                            Log.e(TAG, "Network transport failure during exchange transaction phase", t);
                        }
                    }
                });
    }

    private synchronized void establishWebSocketConnection(String shortLivedToken) {
        if (webSocket != null) return;

        isConnecting = true;

        Request request = new Request.Builder()
                .url(WS_URL)
                .addHeader("Authorization", "Bearer " + shortLivedToken)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                synchronized (ChatWebSocketManager.this) {
                    isConnecting = false;
                }
                Log.d(TAG, "Netty handshake secure! Pipeline online.");
                notifyStateChanged(true);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d(TAG, "Incoming streaming payload: " + text);
                packetDispatcher.dispatch(text);
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                ws.close(1000, null);
                Log.d(TAG, "Connection clean tear-down requested: " + reason);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                clearSocketState();
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WebSocket pipeline collapsed: " + t.getMessage(), t);
                clearSocketState();
            }
        });
    }

    public synchronized void sendMessage(String jsonPayload) {
        if (webSocket != null) {
            webSocket.send(jsonPayload);
        } else {
            Log.w(TAG, "Cannot send message. WebSocket connection is offline.");
        }
    }

    public synchronized void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "App backgrounded / User action");
            webSocket = null;
        }
        clearSocketState();
    }

    private void clearSocketState() {
        synchronized (this) {
            webSocket = null;
            isConnecting = false;
            isExchangingToken = false;
        }
        notifyStateChanged(false);
    }

    private void notifyStateChanged(boolean isConnected) {
        if (messageListener != null) {
            new Handler(Looper.getMainLooper()).post(() -> messageListener.onConnectionStateChanged(isConnected));
        }
    }
}