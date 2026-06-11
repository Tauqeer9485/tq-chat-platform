package com.tq.distributed_chat_android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.data.model.Conversation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private final List<Conversation> conversationList;
    private final OnConversationClickListener clickListener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(List<Conversation> conversationList, OnConversationClickListener clickListener) {
        this.conversationList = conversationList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.bind(conversation, clickListener);
    }

    @Override
    public int getItemCount() {
        return conversationList != null ? conversationList.size() : 0;
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameText;
        private final TextView lastMessageText;
        private final TextView timeText;
        private final ImageView profileImage;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            lastMessageText = itemView.findViewById(R.id.lastMessageText);
            timeText = itemView.findViewById(R.id.timeText);
            profileImage = itemView.findViewById(R.id.profileImage);
        }

        public void bind(final Conversation conversation, final OnConversationClickListener listener) {
            usernameText.setText(conversation.getParticipantName());
            lastMessageText.setText(conversation.getLastMessage());

            timeText.setText(formatTime(conversation.getTimestamp()));

            if (profileImage != null) {
                if (conversation.isGroupChat()) {
                    profileImage.setImageResource(R.drawable.ic_shutter);
                } else {
                    profileImage.setImageResource(R.drawable.ic_user_placeholder);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }

        private String formatTime(long timestamp) {
            if (timestamp == 0) return "";
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
