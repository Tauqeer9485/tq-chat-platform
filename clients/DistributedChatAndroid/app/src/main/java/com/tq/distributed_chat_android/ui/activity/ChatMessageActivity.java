package com.tq.distributed_chat_android.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.remote.ChatWebSocketManager;
import com.tq.distributed_chat_android.data.repository.MessageRepository;
import com.tq.distributed_chat_android.ui.adapter.ChatAdapter;
import com.tq.distributed_chat_android.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageActivity extends AppCompatActivity {
    private EditText messageInput;
    private ImageView btnMic, btnSend;
    private RecyclerView messageRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private final String currentUserId = SessionManager.getUserId();
    private String activeChatRoomId;
    private ChatWebSocketManager webSocketManager;
    private MessageRepository messageRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_area);

        webSocketManager = ChatWebSocketManager.getInstance(this, null);
        messageRepository = MessageRepository.getInstance(this);

        String targetName = getIntent().getStringExtra("PARTICIPANT_NAME");
        if (targetName == null) targetName = "Secure Chat Node";

        activeChatRoomId = getIntent().getStringExtra("CONVERSATION_ID");
        if (activeChatRoomId == null) {
            activeChatRoomId = "default_fallback_room_id";
        }

        Toolbar toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(targetName);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        messageInput = findViewById(R.id.messageInput);
        btnMic = findViewById(R.id.btn_mic);
        btnSend = findViewById(R.id.btn_send);
        messageRecyclerView = findViewById(R.id.messageRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(layoutManager);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        messageRecyclerView.setAdapter(chatAdapter);

        messageRepository.getMessagesForConversation(activeChatRoomId).observe(this, newMessages -> {
            if (newMessages != null) {
                messageList.clear();
                messageList.addAll(newMessages);

                chatAdapter.notifyDataSetChanged();

                if (!messageList.isEmpty()) {
                    messageRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }
        });

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    btnMic.setVisibility(View.GONE);
                    btnSend.setVisibility(View.VISIBLE);
                } else {
                    btnMic.setVisibility(View.VISIBLE);
                    btnSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSend.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {

                long transientMessageId = System.currentTimeMillis();
                String clientMsgId = "client_msg_" + java.util.UUID.randomUUID().toString();

                ChatMessage newTextMessage = new ChatMessage(
                        transientMessageId,
                        activeChatRoomId,
                        currentUserId,
                        clientMsgId,
                        System.currentTimeMillis(),
                        ChatMessage.ContentType.TEXT,
                        text,
                        null,
                        null,
                        null,
                        0,
                        ChatMessage.MessageStatus.SENT
                );

                messageRepository.insertMessage(newTextMessage);

                messageInput.setText("");

                JsonObject jsonObject = new Gson().toJsonTree(newTextMessage).getAsJsonObject();
                jsonObject.addProperty("packetType", "CHAT_MESSAGE");

                webSocketManager.sendMessage(jsonObject.toString());
            }
        });
    }
}
