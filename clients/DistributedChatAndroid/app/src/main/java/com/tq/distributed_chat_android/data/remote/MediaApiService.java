package com.tq.distributed_chat_android.data.remote;

import com.tq.distributed_chat_android.data.remote.dto.MediaUploadResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface MediaApiService {

    @Multipart
    @POST("api/media/upload")
    Call<MediaUploadResponse> uploadFile(
            @Header("Authorization") String shortLivedBearerToken,
            @Part MultipartBody.Part file,
            @Part("conversationId") RequestBody conversationId,
            @Part("uploaderId") RequestBody uploaderId
    );
}