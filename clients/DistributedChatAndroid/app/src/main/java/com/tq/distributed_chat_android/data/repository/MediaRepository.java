package com.tq.distributed_chat_android.data.repository;

import android.net.Uri;
import com.tq.distributed_chat_android.data.remote.dto.MediaUploadResponse;
import retrofit2.Callback;

public interface MediaRepository {
    void uploadMediaStream(
            Uri fileUri,
            String conversationId,
            String uploaderId,
            String shortLivedToken,
            Callback<MediaUploadResponse> callback
    );
}