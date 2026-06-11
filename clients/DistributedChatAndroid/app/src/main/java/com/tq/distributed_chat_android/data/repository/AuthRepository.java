package com.tq.distributed_chat_android.data.repository;

import com.tq.distributed_chat_android.data.remote.ApiClient;
import com.tq.distributed_chat_android.data.remote.AuthApiService;
import com.tq.distributed_chat_android.data.remote.dto.AuthResponse;
import com.tq.distributed_chat_android.data.remote.dto.LoginRequest;
import com.tq.distributed_chat_android.data.remote.dto.SignUpRequest;
import com.tq.distributed_chat_android.data.remote.dto.TokenVerifyRequest;
import com.tq.distributed_chat_android.data.remote.dto.VerifyResponse;

import retrofit2.Callback;

public class AuthRepository {
    private final AuthApiService apiService;

    public AuthRepository() {
        this.apiService = ApiClient.getAuthService();
    }

    public void loginUser(String username, String password, Callback<AuthResponse> callback) {
        LoginRequest payload = new LoginRequest(username, password);
        apiService.login(payload).enqueue(callback);
    }

    public void registerUser(String username, String email, String password, Callback<AuthResponse> callback) {
        SignUpRequest payload = new SignUpRequest(username, email, password, password);
        apiService.signup(payload).enqueue(callback);
    }

    public void verifySession(String token, Callback<VerifyResponse> callback) {
        TokenVerifyRequest payload = new TokenVerifyRequest(token);
        apiService.verifyToken(payload).enqueue(callback);
    }
}