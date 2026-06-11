package com.tq.distributed_chat_android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.data.model.SearchUser;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    private final List<SearchUser> searchUserList;
    private final OnSearchUserClickListener clickListener;

    public interface OnSearchUserClickListener {
        void onSearchClick(SearchUser searchUser);
    }

    public SearchAdapter(List<SearchUser> searchUserList, OnSearchUserClickListener clickListener) {
        this.searchUserList = searchUserList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchUser searchUser = searchUserList.get(position);
        holder.bind(searchUser, clickListener);
    }

    @Override
    public int getItemCount() {
        return searchUserList != null ? searchUserList.size() : 0;
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameText;
        private final TextView lastMessageText;
        private final TextView timeText;
        private final ImageView profileImage;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            lastMessageText = itemView.findViewById(R.id.lastMessageText);
            timeText = itemView.findViewById(R.id.timeText);
            profileImage = itemView.findViewById(R.id.profileImage);
        }

        public void bind(final SearchUser searchUser, final OnSearchUserClickListener listener) {
            usernameText.setText(searchUser.getUsername());

            lastMessageText.setText("Tap to start secure connection");
            timeText.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSearchClick(searchUser);
                }
            });
        }
    }
}
