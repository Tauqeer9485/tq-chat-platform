package com.tq.distributed_chat_android.data.remote;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.tq.distributed_chat_android.data.model.AttachmentType;
import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.model.MediaMetadata;
import com.tq.distributed_chat_android.data.remote.dto.MediaUploadResponse;
import com.tq.distributed_chat_android.data.remote.dto.TokenExchangeRequest;
import com.tq.distributed_chat_android.data.remote.dto.TokenExchangeResponse;
import com.tq.distributed_chat_android.data.repository.MediaRepository;
import com.tq.distributed_chat_android.data.repository.MessageRepository;
import com.tq.distributed_chat_android.data.mapper.MessageMapper;
import com.tq.distributed_chat_android.data.protocol.chat.MessagePacket;

import java.util.Collections;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatMediaUploadManager {
    private static final String TAG = "ChatMediaUploadManager";

    private final Context context;
    private final MediaRepository mediaRepository;
    private final ChatWebSocketManager webSocketManager;
    private final MessageRepository messageRepository;

    public ChatMediaUploadManager(Context context, MediaRepository mediaRepository,
                                  ChatWebSocketManager webSocketManager, MessageRepository messageRepository) {
        this.context = context.getApplicationContext();
        this.mediaRepository = mediaRepository;
        this.webSocketManager = webSocketManager;
        this.messageRepository = messageRepository;
    }

    public void processMediaMessageSend(MediaUploadTask task, String conversationId, String currentUserId, String longLivedToken) {
        String authHeader = "Bearer " + longLivedToken;
        TokenExchangeRequest requestPayload = new TokenExchangeRequest(Collections.singletonList("media:write"));

        ApiClient.getAuthService().exchangeToken(authHeader, requestPayload)
                .enqueue(new Callback<TokenExchangeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TokenExchangeResponse> call, @NonNull Response<TokenExchangeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String shortLivedMediaToken = response.body().getToken();
                            executeMediaUpload(task, conversationId, currentUserId, shortLivedMediaToken);
                        } else {
                            Log.e(TAG, "Media token exchange rejected. HTTP Code: " + response.code());
                            Toast.makeText(context, "Security verification failed for file attachment.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<TokenExchangeResponse> call, Throwable t) {
                        Log.e(TAG, "Transport failure during media authorization phase", t);
                        Toast.makeText(context, "Network error during authentication sync.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void executeMediaUpload(MediaUploadTask task, String conversationId, String currentUserId, String shortLivedToken) {
        mediaRepository.uploadMediaStream(task.getFileUri(), conversationId, currentUserId, shortLivedToken, new Callback<MediaUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<MediaUploadResponse> call, @NonNull Response<MediaUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MediaUploadResponse uploadData = response.body();
                    dispatchMediaMessageToRoomAndWebSocket(task, uploadData, conversationId, currentUserId);
                } else {
                    Log.e(TAG, "Media Server rejected file payload. Status: " + response.code());
                    Toast.makeText(context, "File upload rejected by remote cluster asset gateway.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MediaUploadResponse> call, Throwable t) {
                Log.e(TAG, "Fatal network transport block during media payload shipment phase", t);
                Toast.makeText(context, "Failed to stream media asset to remote server.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dispatchMediaMessageToRoomAndWebSocket(MediaUploadTask task, MediaUploadResponse uploadData, String conversationId, String currentUserId) {
        long transientMessageId = System.currentTimeMillis();
        String clientMsgId = "client_msg_" + UUID.randomUUID().toString();

        ChatMessage.ContentType determinedType = determineContentType(uploadData.getMimeType());
        long parsedSize = 0;
        try {
            parsedSize = Long.parseLong(uploadData.getSize());
        } catch (NumberFormatException ignored) {}

        MediaMetadata metadata = task.getPreCalculatedMetadata();
        metadata.setMediaId(uploadData.getMediaId());
        metadata.setDownloadUrl(uploadData.getDownloadUrl());
        metadata.setFileName(uploadData.getFileName());
        metadata.setFileSize(parsedSize);
        metadata.setMimeType(uploadData.getMimeType());

        ChatMessage mediaMessage = new ChatMessage(
                transientMessageId,
                conversationId,
                currentUserId,
                clientMsgId,
                System.currentTimeMillis(),
                determinedType,
                "[Media Attachment: " + uploadData.getFileName() + "]",
                null,
                ChatMessage.MessageStatus.SENT,
                AttachmentType.MEDIA,
                metadata
        );

        MessagePacket structuralPacket = MessageMapper.toPacket(mediaMessage);
        if (webSocketManager != null && structuralPacket != null) {
            webSocketManager.sendMessage(new Gson().toJson(structuralPacket));
        }

        mediaMessage.getMediaMetadata().setDownloadUrl(task.getFileUri().toString());
        messageRepository.insertMessage(mediaMessage);
    }

    private ChatMessage.ContentType determineContentType(String mimeType) {
        if (mimeType == null) return ChatMessage.ContentType.FILE;
        String primaryType = mimeType.split("/")[0].toLowerCase();
        try {
            return ChatMessage.ContentType.valueOf(primaryType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChatMessage.ContentType.FILE;
        }
    }
}