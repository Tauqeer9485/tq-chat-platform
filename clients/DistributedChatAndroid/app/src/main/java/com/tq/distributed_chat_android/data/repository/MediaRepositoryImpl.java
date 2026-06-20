package com.tq.distributed_chat_android.data.repository;

import android.content.Context;
import android.net.Uri;
import com.tq.distributed_chat_android.data.remote.MediaApiService;
import com.tq.distributed_chat_android.data.remote.dto.MediaUploadResponse;
import com.tq.distributed_chat_android.data.remote.dto.ContentUriRequest;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Callback;

public class MediaRepositoryImpl implements MediaRepository {
    private final Context context;
    private final MediaApiService mediaApiService;

    public MediaRepositoryImpl(Context context, MediaApiService mediaApiService) {
        this.context = context.getApplicationContext();
        this.mediaApiService = mediaApiService;
    }

    @Override
    public void uploadMediaStream(Uri fileUri, String conversationId, String uploaderId, String shortLivedToken, Callback<MediaUploadResponse> callback) {
        ContentUriRequest fileRequestBody = new ContentUriRequest(context, fileUri);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "file",
                fileRequestBody.getFileName(),
                fileRequestBody
        );

        RequestBody convIdPart = RequestBody.create(MediaType.parse("text/plain"), conversationId);
        RequestBody uploaderIdPart = RequestBody.create(MediaType.parse("text/plain"), uploaderId);

        mediaApiService.uploadFile("Bearer " + shortLivedToken, filePart, convIdPart, uploaderIdPart).enqueue(callback);
    }
}