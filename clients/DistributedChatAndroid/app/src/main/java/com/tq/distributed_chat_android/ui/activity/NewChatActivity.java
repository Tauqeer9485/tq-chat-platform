package com.tq.distributed_chat_android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.data.model.Conversation;
import com.tq.distributed_chat_android.data.repository.ConversationRepository;
import com.tq.distributed_chat_android.util.SessionManager;
import com.tq.distributed_chat_android.data.model.SearchUser;
import com.tq.distributed_chat_android.data.remote.dto.UserSearchResponse;
import com.tq.distributed_chat_android.data.remote.ApiClient;
import com.tq.distributed_chat_android.data.remote.dto.DirectChatRequest;
import com.tq.distributed_chat_android.data.remote.dto.DirectChatResponse;
import com.tq.distributed_chat_android.ui.adapter.SearchAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewChatActivity extends AppCompatActivity implements SearchAdapter.OnSearchUserClickListener {
    private TextInputEditText searchInput;
    private RecyclerView usersRecyclerView;
    private SearchAdapter adapter;
    private List<SearchUser> filteredList;
    private SessionManager sessionManager;
    private ConversationRepository conversationRepository;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<List<UserSearchResponse>> currentSearchCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        sessionManager = new SessionManager(this);
        conversationRepository = ConversationRepository.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        searchInput = findViewById(R.id.searchInput);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        FloatingActionButton fabBack = findViewById(R.id.fabBack);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        fabBack.setOnClickListener(v -> finish());

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        filteredList = new ArrayList<>();
        adapter = new SearchAdapter(filteredList, this);
        usersRecyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (currentSearchCall != null && !currentSearchCall.isCanceled()) {
                    currentSearchCall.cancel();
                }

                String query = s.toString().trim();

                searchRunnable = () -> {
                    if (query.length() >= 2) {
                        executeUserSearch(query);
                    } else {
                        updateUiWithResults(new ArrayList<>());
                    }
                };

                searchHandler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void executeUserSearch(String query) {
        String jwtToken = sessionManager.getAuthToken();
        if (jwtToken == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorizationHeader = "Bearer " + jwtToken;

        currentSearchCall = ApiClient.getAuthService().searchUsers(authorizationHeader, query);
        currentSearchCall.enqueue(new Callback<List<UserSearchResponse>>() {
            @Override
            public void onResponse(Call<List<UserSearchResponse>> call, Response<List<UserSearchResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SearchUser> searchResults = new ArrayList<>();
                    for (UserSearchResponse remoteUser : response.body()) {
                        searchResults.add(new SearchUser(
                                remoteUser.getUserId(),
                                remoteUser.getUsername(),
                                remoteUser.getProfilePicture()
                        ));
                    }
                    updateUiWithResults(searchResults);
                }
            }

            @Override
            public void onFailure(Call<List<UserSearchResponse>> call, Throwable t) {
                if (!call.isCanceled()) {
                    Toast.makeText(NewChatActivity.this, "Search error encountered", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUiWithResults(List<SearchUser> newUsers) {
        filteredList.clear();
        filteredList.addAll(newUsers);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSearchClick(SearchUser userNode) {
        String jwtToken = sessionManager.getAuthToken();
        if (jwtToken == null) {
            Toast.makeText(this, "Session expired, authorization missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorizationHeader = "Bearer " + jwtToken;
        DirectChatRequest payload = new DirectChatRequest(userNode.getUserId());

        Toast.makeText(this, "Opening connection to remote node...", Toast.LENGTH_SHORT).show();

        ApiClient.getAuthService().createDirectConversation(authorizationHeader, payload)
                .enqueue(new Callback<DirectChatResponse>() {
                    @Override
                    public void onResponse(Call<DirectChatResponse> call, Response<DirectChatResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String activeChatRoomId = response.body().getConversationId();
                            Toast.makeText(NewChatActivity.this, "Handshake secure! Opening room.", Toast.LENGTH_SHORT).show();

                            Conversation conversation = new Conversation(
                                    activeChatRoomId,
                                    userNode.getUsername(),
                                    "Send a message to starting the chat log...",
                                    System.currentTimeMillis(),
                                    Conversation.ConversationType.DIRECT
                            );

                            conversationRepository.addConversation(conversation);

                            Intent intent = new Intent(NewChatActivity.this, ChatMessageActivity.class);
                            intent.putExtra("CONVERSATION_ID", activeChatRoomId);
                            intent.putExtra("PARTICIPANT_NAME", userNode.getUsername());
                            intent.putExtra("CONVERSATION_TYPE", Conversation.ConversationType.DIRECT.name());

                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(NewChatActivity.this, "Handshake failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectChatResponse> call, Throwable t) {
                        Toast.makeText(NewChatActivity.this, "Network error contacting Messaging cluster node", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        if (currentSearchCall != null) {
            currentSearchCall.cancel();
        }
    }
}
