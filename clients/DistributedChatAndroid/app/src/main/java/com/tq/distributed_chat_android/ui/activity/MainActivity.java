package com.tq.distributed_chat_android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.data.remote.ChatWebSocketManager;
import com.tq.distributed_chat_android.util.SessionManager;
import com.tq.distributed_chat_android.data.remote.ApiClient;
import com.tq.distributed_chat_android.data.remote.dto.AuthResponse;
import com.tq.distributed_chat_android.data.remote.dto.LoginRequest;
import com.tq.distributed_chat_android.data.remote.dto.SignUpRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnSubmit;
    private TextView tvToggleMode, tvTitle, tvSubtitle;
    private ProgressBar progressBar;

    private boolean isLoginMode = true;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        progressBar = findViewById(R.id.progressBar);

        tvToggleMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoginMode = !isLoginMode;
                if (isLoginMode) {
                    tvTitle.setText("Distributed Chat");
                    tvSubtitle.setText("Connect to your microservice node");
                    etEmail.setVisibility(View.GONE);
                    etConfirmPassword.setVisibility(View.GONE); // Hide
                    btnSubmit.setText("Sign In");
                    tvToggleMode.setText("Don't have an account? Sign Up");
                } else {
                    tvTitle.setText("Create Account");
                    tvSubtitle.setText("Register a new node identity");
                    etEmail.setVisibility(View.VISIBLE);
                    etConfirmPassword.setVisibility(View.VISIBLE); // Show
                    btnSubmit.setText("Register");
                    tvToggleMode.setText("Already have an account? Sign In");
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isLoginMode) {
                    performLogin(username, password);
                } else {
                    String email = etEmail.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();

                    if (email.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Email required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!password.equals(confirmPassword)) {
                        Toast.makeText(MainActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    performSignUp(username, email, password);
                }
            }
        });
    }

    private void performLogin(String u, String p) {
        setLoading(true);
        ApiClient.getAuthService().login(new LoginRequest(u, p)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> res) {
                setLoading(false);
                if (res.isSuccessful() && res.body() != null) {
                    AuthResponse.UserDetails user = res.body().getUser();

                    sessionManager.saveAuthToken(res.body().getToken());

                    sessionManager.saveUser(
                            user.getUserId(),
                            user.getUsername(),
                            user.getEmail()
                    );

                    ChatWebSocketManager.getInstance(getApplicationContext(), null).connectWithAuthentication(res.body().getToken());

                    navigateToHome();
                } else {
                    Toast.makeText(MainActivity.this, "Auth failed: " + res.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSignUp(String u, String e, String p) {
        setLoading(true);

        SignUpRequest payload = new SignUpRequest(u, e, p, p);

        ApiClient.getAuthService().signup(payload).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> res) {
                setLoading(false);
                if (res.isSuccessful() && res.body() != null) {
                    Toast.makeText(MainActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Registration failed: " + res.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean load) {
        progressBar.setVisibility(load ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!load);
    }
}