package com.tq.distributed_chat_android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.data.model.Conversation;
import com.tq.distributed_chat_android.data.repository.ConversationRepository;
import com.tq.distributed_chat_android.ui.adapter.ConversationAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements ConversationAdapter.OnConversationClickListener {
    private RecyclerView rvConversations;
    private ConversationAdapter adapter;
    private List<Conversation> dataList;
    private ConversationRepository conversationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        conversationRepository = ConversationRepository.getInstance(this);

        rvConversations = findViewById(R.id.rvConversations);
        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);

        rvConversations.setLayoutManager(new LinearLayoutManager(this));

        dataList = new ArrayList<>();
        adapter = new ConversationAdapter(dataList, this);
        rvConversations.setAdapter(adapter);

        conversationRepository.getConversations().observe(this, conversations -> {
            if (conversations != null) {
                dataList.clear();
                dataList.addAll(conversations);

                adapter.notifyDataSetChanged();
            }
        });

        fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NewChatActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        Intent intent = new Intent(HomeActivity.this, ChatMessageActivity.class);

        intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
        intent.putExtra("PARTICIPANT_NAME", conversation.getParticipantName());

        intent.putExtra("CONVERSATION_TYPE", conversation.getConversationType().name());

        startActivity(intent);
    }
}
