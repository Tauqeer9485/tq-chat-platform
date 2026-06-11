package com.tq.distributed_chat_android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.tq.distributed_chat_android.util.SessionManager;
import com.tq.distributed_chat_android.data.remote.ApiClient;
import com.tq.distributed_chat_android.data.remote.dto.VerifyResponse;
import com.tq.distributed_chat_android.data.repository.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private AuthRepository authRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);

        String savedToken = sessionManager.getAuthToken();

        if (savedToken == null) {
            redirectToLogin();
        } else {
            verifySavedSession(savedToken);
        }
    }

    private void verifySavedSession(String token) {
        authRepository.verifySession(token, new Callback<VerifyResponse>() {
            @Override
            public void onResponse(Call<VerifyResponse> call, Response<VerifyResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiClient.setAuthToken(token);
                    redirectToHome();
                } else {
                    sessionManager.clearSession();
                    Toast.makeText(SplashActivity.this, "Session expired", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                }
            }

            @Override
            public void onFailure(Call<VerifyResponse> call, Throwable t) {
                Toast.makeText(SplashActivity.this, "Server Node Offline", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        });
    }

    private void redirectToHome() {
        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        finish();
    }

    private void redirectToLogin() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
}