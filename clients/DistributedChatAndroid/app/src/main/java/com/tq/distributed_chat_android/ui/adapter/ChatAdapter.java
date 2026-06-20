package com.tq.distributed_chat_android.ui.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.data.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    private final String currentUserId;
    private final OnAudioMessageClickListener audioClickListener;

    private static final int TYPE_TEXT_SENT_SHORT     = 1;
    private static final int TYPE_TEXT_SENT_LONG      = 2;
    private static final int TYPE_TEXT_RECEIVED_SHORT = 3;
    private static final int TYPE_TEXT_RECEIVED_LONG  = 4;
    private static final int TYPE_IMAGE_SENT          = 5;
    private static final int TYPE_IMAGE_RECEIVED      = 6;
    private static final int TYPE_AUDIO_SENT          = 7;
    private static final int TYPE_AUDIO_RECEIVED      = 8;

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatMessage>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                    if (oldItem.getClientMessageId() != null && newItem.getClientMessageId() != null) {
                        return oldItem.getClientMessageId().equals(newItem.getClientMessageId());
                    }
                    return oldItem.getTimestamp() == newItem.getTimestamp();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                    boolean contentMatches = (oldItem.getContent() == null && newItem.getContent() == null) ||
                            (oldItem.getContent() != null && oldItem.getContent().equals(newItem.getContent()));
                    boolean statusMatches = oldItem.getStatus().equals(newItem.getStatus());

                    boolean mediaMatches = (oldItem.getMediaUrl() == null && newItem.getMediaUrl() == null) ||
                            (oldItem.getMediaUrl() != null && oldItem.getMediaUrl().equals(newItem.getMediaUrl()));

                    return contentMatches && statusMatches && mediaMatches && oldItem.getTimestamp() == newItem.getTimestamp();
                }
            };

    public ChatAdapter(String currentUserId, OnAudioMessageClickListener audioClickListener) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
        this.audioClickListener = audioClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        boolean isSent = message.getSenderId().equals(currentUserId);

        if (message.getContentType() == ChatMessage.ContentType.IMAGE) {
            return isSent ? TYPE_IMAGE_SENT : TYPE_IMAGE_RECEIVED;
        } else if (message.getContentType() == ChatMessage.ContentType.AUDIO) {
            return isSent ? TYPE_AUDIO_SENT : TYPE_AUDIO_RECEIVED;
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
            case TYPE_AUDIO_SENT:
                return new AudioViewHolder(inflater.inflate(R.layout.item_chat_audio_sent, parent, false));
            case TYPE_AUDIO_RECEIVED:
                return new AudioViewHolder(inflater.inflate(R.layout.item_chat_audio_received, parent, false));
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
                throw new IllegalArgumentException("Unhandled ViewType");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = getItem(position);
        boolean isMe = message.getSenderId().equals(currentUserId);

        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(message, isMe);
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(message, isMe);
        } else if (holder instanceof AudioViewHolder) {
            ((AudioViewHolder) holder).bind(message, isMe, audioClickListener);
        }
    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder {
        public final ImageView btnPlayPause;
        public final SeekBar audioSeekBar;
        public final TextView txtDuration;
        public final TextView timeText;
        public final ImageView statusImage;
        public final View downloadContainer;
        public final ProgressBar downloadProgress;
        public final TextView downloadPercent;

        AudioViewHolder(View view) {
            super(view);
            btnPlayPause = view.findViewById(R.id.btnPlayPause);
            audioSeekBar = view.findViewById(R.id.audioSeekBar);
            txtDuration = view.findViewById(R.id.txtDuration);
            timeText = view.findViewById(R.id.timeText);
            statusImage = view.findViewById(R.id.statusImage);
            downloadContainer = view.findViewById(R.id.downloadContainer);
            downloadProgress = view.findViewById(R.id.downloadProgress);
            downloadPercent = view.findViewById(R.id.downloadPercent);
        }

        void bind(ChatMessage message, boolean isSentByUser, OnAudioMessageClickListener listener) {
            timeText.setText(formatTime(message.getTimestamp()));

            String playableUrl = message.getMediaUrl();
            if (playableUrl != null && playableUrl.contains("localhost")) {
                playableUrl = playableUrl.replace("localhost", "192.168.43.248");
            }

            boolean isLocalFile = playableUrl != null && (
                    playableUrl.startsWith("content://") ||
                            playableUrl.startsWith("file://") ||
                            playableUrl.startsWith("/")
            );

            if (statusImage != null) {
                if (isSentByUser && message.getStatus() != null) {
                    statusImage.setVisibility(View.VISIBLE);
                    switch (message.getStatus()) {
                        case PENDING: statusImage.setVisibility(View.GONE); break;
                        case SENT: statusImage.setImageResource(R.drawable.ic_tick_sent); break;
                        case DELIVERED: statusImage.setImageResource(R.drawable.ic_ticks_delivered); break;
                        case READ: statusImage.setImageResource(R.drawable.ic_ticks_seen); break;
                        case FAILED: statusImage.setImageResource(android.R.drawable.stat_notify_error); break;
                    }
                } else {
                    statusImage.setVisibility(View.GONE);
                }
            }

            downloadContainer.setVisibility(View.GONE);

            if (isLocalFile) {
                btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_download);
            }

            btnPlayPause.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAudioMessageClick(message, this);
                }
            });

            audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
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
                        case PENDING: statusIcon.setVisibility(View.GONE); break;
                        case SENT: statusIcon.setImageResource(R.drawable.ic_tick_sent); break;
                        case DELIVERED: statusIcon.setImageResource(R.drawable.ic_ticks_delivered); break;
                        case READ: statusIcon.setImageResource(R.drawable.ic_ticks_seen); break;
                        case FAILED: statusIcon.setImageResource(android.R.drawable.stat_notify_error); break;
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
        private final View progressContainer;
        private final View retryContainer;
        private final ProgressBar progressBar;
        private final TextView progressText;
        private final ImageView btnRetry;
        private final ImageView btnCancel;

        ImageViewHolder(View view) {
            super(view);
            messageImage = view.findViewById(R.id.imageView);
            timeText = view.findViewById(R.id.timeText);
            statusImage = view.findViewById(R.id.statusImage);
            overlay = view.findViewById(R.id.overlay);
            progressContainer = view.findViewById(R.id.progressContainer);
            retryContainer = view.findViewById(R.id.retryContainer);
            progressBar = view.findViewById(R.id.progressBar);
            progressText = view.findViewById(R.id.progressText);
            btnRetry = view.findViewById(R.id.btnRetry);
            btnCancel = view.findViewById(R.id.btnCancel);
        }

        void bind(ChatMessage message, boolean isSentByUser) {
            timeText.setText(formatTime(message.getTimestamp()));

            if (statusImage != null) {
                if (isSentByUser && message.getStatus() != null) {
                    statusImage.setVisibility(View.VISIBLE);
                    switch (message.getStatus()) {
                        case PENDING: statusImage.setVisibility(View.GONE); break;
                        case SENT: statusImage.setImageResource(R.drawable.ic_tick_sent); break;
                        case DELIVERED: statusImage.setImageResource(R.drawable.ic_ticks_delivered); break;
                        case READ: statusImage.setImageResource(R.drawable.ic_ticks_seen); break;
                        case FAILED: statusImage.setImageResource(android.R.drawable.stat_notify_error); break;
                    }
                } else {
                    statusImage.setVisibility(View.GONE);
                }
            }

            if (overlay != null) {
                if (message.getStatus() == ChatMessage.MessageStatus.PENDING) {
                    overlay.setVisibility(View.VISIBLE);
                    if (progressContainer != null) progressContainer.setVisibility(View.VISIBLE);
                    if (retryContainer != null) retryContainer.setVisibility(View.GONE);
                    if (progressBar != null) progressBar.setIndeterminate(true);
                    if (progressText != null) progressText.setText("Uploading...");
                } else if (message.getStatus() == ChatMessage.MessageStatus.FAILED) {
                    overlay.setVisibility(View.VISIBLE);
                    if (progressContainer != null) progressContainer.setVisibility(View.GONE);
                    if (retryContainer != null) retryContainer.setVisibility(View.VISIBLE);
                } else {
                    overlay.setVisibility(View.GONE);
                }
            }

            String contentPath = message.getContent();
            String remoteUrl = message.getMediaUrl();

            String fixedRemoteUrl = null;
            if (remoteUrl != null) {
                fixedRemoteUrl = remoteUrl.replace("localhost", "192.168.43.248");
            }

            boolean isLocalPath = contentPath != null && (contentPath.startsWith("content://") || contentPath.startsWith("file://") || contentPath.startsWith("/"));

            if (isLocalPath) {
                Glide.with(itemView.getContext())
                        .load(Uri.parse(contentPath))
                        .transform(new RoundedCorners(16))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .error(Glide.with(itemView.getContext())
                                .load(fixedRemoteUrl)
                                .transform(new RoundedCorners(16))
                                .placeholder(R.drawable.ic_gallery))
                        .into(messageImage);
            } else if (fixedRemoteUrl != null && !fixedRemoteUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(fixedRemoteUrl)
                        .transform(new RoundedCorners(16))
                        .placeholder(R.drawable.ic_gallery)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(messageImage);
            } else {
                messageImage.setImageResource(R.drawable.ic_gallery);
            }
        }
    }

    private static String formatTime(long timestamp) {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public interface OnAudioMessageClickListener {
        void onAudioMessageClick(ChatMessage message, AudioViewHolder holder);
    }
}
