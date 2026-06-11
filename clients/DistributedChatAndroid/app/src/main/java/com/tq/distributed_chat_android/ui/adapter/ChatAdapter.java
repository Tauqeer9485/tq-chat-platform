package com.tq.distributed_chat_android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.data.model.ChatMessage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> messages;
    private final String currentUserId;
    private static final int TYPE_TEXT_SENT_SHORT     = 1;
    private static final int TYPE_TEXT_SENT_LONG      = 2;
    private static final int TYPE_TEXT_RECEIVED_SHORT = 3;
    private static final int TYPE_TEXT_RECEIVED_LONG  = 4;
    private static final int TYPE_IMAGE_SENT          = 5;
    private static final int TYPE_IMAGE_RECEIVED      = 6;

    public ChatAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        boolean isSent = message.getSenderId().equals(currentUserId);

        if (message.getContentType() == ChatMessage.ContentType.IMAGE) {
            return isSent ? TYPE_IMAGE_SENT : TYPE_IMAGE_RECEIVED;
        } else {
            boolean isShort = message.getContent() == null || message.getContent().length() <= 20;
            if (isSent) {
                return isShort ? TYPE_TEXT_SENT_SHORT : TYPE_TEXT_SENT_LONG;
            } else {
                return isShort ? TYPE_TEXT_RECEIVED_SHORT : TYPE_TEXT_RECEIVED_LONG;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_IMAGE_SENT:
                return new ImageViewHolder(inflater.inflate(R.layout.item_chat_image_sent, parent, false));
            case TYPE_IMAGE_RECEIVED:
                return new ImageViewHolder(inflater.inflate(R.layout.item_chat_image_received, parent, false));
            case TYPE_TEXT_SENT_SHORT:
                return new TextViewHolder(inflater.inflate(R.layout.item_chat_text_sent_short, parent, false));
            case TYPE_TEXT_SENT_LONG:
                return new TextViewHolder(inflater.inflate(R.layout.item_chat_text_sent, parent, false));
            case TYPE_TEXT_RECEIVED_SHORT:
                return new TextViewHolder(inflater.inflate(R.layout.item_chat_text_received_short, parent, false));
            case TYPE_TEXT_RECEIVED_LONG:
                return new TextViewHolder(inflater.inflate(R.layout.item_chat_text_received, parent, false));
            default:
                throw new IllegalArgumentException("Unhandled ViewType layout structural mapping requested.");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        boolean isMe = message.getSenderId().equals(currentUserId);

        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(message, isMe);
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(message, isMe);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView timeText;
        private final ImageView statusIcon;

        TextViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.messageText);
            timeText = view.findViewById(R.id.timeText);
            statusIcon = view.findViewById(R.id.statusIcon);
        }

        void bind(ChatMessage message, boolean isSentByUser) {
            messageText.setText(message.getContent());
            timeText.setText(formatTime(message.getTimestamp()));

            if (statusIcon != null) {
                if (isSentByUser && message.getStatus() != null) {
                    statusIcon.setVisibility(View.VISIBLE);
                    switch (message.getStatus()) {
//                        case PENDING:
//                            statusIcon.setImageResource(R.drawable.ic_clock_pending);
//                            break;
                        case SENT:
                            statusIcon.setImageResource(R.drawable.ic_tick_sent);
                            break;
                        case DELIVERED:
                            statusIcon.setImageResource(R.drawable.ic_ticks_delivered);
                            break;
                        case READ:
                            statusIcon.setImageResource(R.drawable.ic_ticks_seen);
                            break;
                        case FAILED:
                            statusIcon.setImageResource(android.R.drawable.stat_notify_error);
                            break;
                    }
                } else {
                    statusIcon.setVisibility(View.GONE);
                }
            }
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView messageImage;
        private final TextView timeText;
        private final ImageView statusImage;
        private final View overlay;
        private final ProgressBar progressBar;
        private final ImageView btnRetry;

        ImageViewHolder(View view) {
            super(view);
            messageImage = view.findViewById(R.id.imageView);
            timeText = view.findViewById(R.id.timeText);
            statusImage = view.findViewById(R.id.statusImage);
            overlay = view.findViewById(R.id.overlay);
            progressBar = view.findViewById(R.id.progressBar);
            btnRetry = view.findViewById(R.id.btnRetry);
        }

        void bind(ChatMessage message, boolean isSentByUser) {
            timeText.setText(formatTime(message.getTimestamp()));

            // Reset media upload UI elements gracefully
            if (overlay != null) overlay.setVisibility(View.GONE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (btnRetry != null) btnRetry.setVisibility(View.GONE);

            // Dynamic status updates for media cards
            if (statusImage != null) {
                if (isSentByUser && message.getStatus() != null) {
                    statusImage.setVisibility(View.VISIBLE);
                    switch (message.getStatus()) {
//                        case PENDING:
//                            statusImage.setImageResource(R.drawable.ic_clock_pending);
//                            break;
                        case SENT:
                            statusImage.setImageResource(R.drawable.ic_tick_sent);
                            break;
                        case DELIVERED:
                            statusImage.setImageResource(R.drawable.ic_ticks_delivered);
                            break;
                        case READ:
                            statusImage.setImageResource(R.drawable.ic_ticks_seen);
                            break;
                        case FAILED:
                            statusImage.setImageResource(android.R.drawable.stat_notify_error);
                            break;
                    }
                } else {
                    statusImage.setVisibility(View.GONE);
                }
            }

            String localPath = message.getContent();
            if (localPath != null && new File(localPath).exists()) {
                Glide.with(itemView.getContext())
                        .load(new File(localPath))
                        .transform(new RoundedCorners(16))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(messageImage);
            } else if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(message.getMediaUrl())
                        .transform(new RoundedCorners(16))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_gallery)
//                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(messageImage);
            }
        }
    }

    private static String formatTime(long timestamp) {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
