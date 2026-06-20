package com.tq.distributed_chat_android.data.remote;

import com.tq.distributed_chat_android.data.remote.dto.AuthResponse;
import com.tq.distributed_chat_android.data.remote.dto.DirectChatRequest;
import com.tq.distributed_chat_android.data.remote.dto.DirectChatResponse;
import com.tq.distributed_chat_android.data.remote.dto.LoginRequest;
import com.tq.distributed_chat_android.data.remote.dto.SignUpRequest;
import com.tq.distributed_chat_android.data.remote.dto.TokenExchangeRequest;
import com.tq.distributed_chat_android.data.remote.dto.TokenExchangeResponse;
import com.tq.distributed_chat_android.data.remote.dto.TokenVerifyRequest;
import com.tq.distributed_chat_android.data.remote.dto.UserSearchResponse;
import com.tq.distributed_chat_android.data.remote.dto.VerifyResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApiService {
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/signup")
    Call<AuthResponse> signup(@Body SignUpRequest request);

    @POST("api/auth/verify")
    Call<VerifyResponse> verifyToken(@Body TokenVerifyRequest request);

    @POST("api/conversations/direct")
    Call<DirectChatResponse> createDirectConversation(
            @Header("Authorization") String bearerToken,
            @Body DirectChatRequest request
    );

    @GET("api/users/search")
    Call<List<UserSearchResponse>> searchUsers(
            @Header("Authorization") String bearerToken,
            @Query("query") String query
    );

    @POST("api/auth/tokens/exchange")
    Call<TokenExchangeResponse> exchangeToken(
            @Header("Authorization") String longLivedBearerToken,
            @Body TokenExchangeRequest request
    );
}