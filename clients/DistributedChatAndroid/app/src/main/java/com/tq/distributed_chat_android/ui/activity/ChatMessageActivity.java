package com.tq.distributed_chat_android.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.data.audio.AndroidAudioRecorderEngine;
import com.tq.distributed_chat_android.data.audio.AudioRecorderEngine;
import com.tq.distributed_chat_android.data.mapper.MessageMapper;
import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.model.MediaMetadata;
import com.tq.distributed_chat_android.data.protocol.chat.MessagePacket;
import com.tq.distributed_chat_android.data.remote.ApiClient;
import com.tq.distributed_chat_android.data.remote.ChatMediaUploadManager;
import com.tq.distributed_chat_android.data.remote.ChatWebSocketManager;
import com.tq.distributed_chat_android.data.remote.MediaApiService;
import com.tq.distributed_chat_android.data.remote.MediaUploadTask;
import com.tq.distributed_chat_android.data.repository.MediaRepository;
import com.tq.distributed_chat_android.data.repository.MediaRepositoryImpl;
import com.tq.distributed_chat_android.data.repository.MessageRepository;
import com.tq.distributed_chat_android.ui.adapter.ChatAdapter;
import com.tq.distributed_chat_android.util.ActiveConversationManager;
import com.tq.distributed_chat_android.util.MediaDownloadEngine;
import com.tq.distributed_chat_android.util.SessionManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatMessageActivity extends AppCompatActivity {
    private EditText messageInput;
    private ImageView btnMic, btnSend, btnAttachment, btnCamera;
    private View micWrapper, micBg;
    private RecyclerView messageRecyclerView;
    private ChatAdapter chatAdapter;
    private final String currentUserId = SessionManager.getUserId();
    private String activeChatRoomId;
    private ChatWebSocketManager webSocketManager;
    private MessageRepository messageRepository;

    private ActivityResultLauncher<String[]> mediaPickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> audioPermissionLauncher;

    private ChatMediaUploadManager mediaUploadManager;
    private AudioRecorderEngine audioRecorderEngine;
    private long recordingStartTime = 0L;
    private boolean isRecordingInProgress = false;
    private long lastClickTime = 0L;
    private static final long DEBOUNCE_DELAY_MS = 500L;
    private static final long MINIMUM_AUDIO_DURATION_MS = 1000L;
    private String longLivedToken;

    private File activeAudioFile;
    private float micStartY;
    private MediaPlayer mediaPlayer;
    private final Handler seekHandler = new Handler();
    private ChatAdapter.AudioViewHolder activeAudioHolder;
    private ChatMessage currentPlayingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_area);

        webSocketManager = ChatWebSocketManager.getInstance(this, null);
        messageRepository = MessageRepository.getInstance(this);
        MediaApiService mediaApiService = ApiClient.getMediaService();
        MediaRepository mediaRepository = new MediaRepositoryImpl(this, mediaApiService);
        mediaUploadManager = new ChatMediaUploadManager(this, mediaRepository, webSocketManager, messageRepository);
        audioRecorderEngine = new AndroidAudioRecorderEngine(this);

        SessionManager sessionManager = new SessionManager(this);
        longLivedToken = sessionManager.getAuthToken();

        String targetName = getIntent().getStringExtra("PARTICIPANT_NAME");
        if (targetName == null) targetName = "Secure Chat Node";

        activeChatRoomId = getIntent().getStringExtra("CONVERSATION_ID");
        if (activeChatRoomId == null) activeChatRoomId = "default_fallback_room_id";

        TextView textView = findViewById(R.id.tvUsername);
        textView.setText(targetName);

        Toolbar toolbar = findViewById(R.id.chatToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        messageInput = findViewById(R.id.messageInput);
        btnMic = findViewById(R.id.btn_mic);
        btnSend = findViewById(R.id.btn_send);
        btnAttachment = findViewById(R.id.btn_attachment);
        btnCamera = findViewById(R.id.btn_camera);
        micWrapper = findViewById(R.id.mic_wrapper);
        micBg = findViewById(R.id.mic_background);
        messageRecyclerView = findViewById(R.id.messageRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messageRecyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(currentUserId, this::handleAudioPlaybackPipeline);
        messageRecyclerView.setAdapter(chatAdapter);

        setupPermissionContracts();
        setupResultContracts();
        setupObservers();
        setupInputListeners();
        setupMediaClickListeners();
        setupAudioTouchPipeline();
    }

    private void handleAudioPlaybackPipeline(ChatMessage message, ChatAdapter.AudioViewHolder holder) {
        String sourceUrl = message.getMediaMetadata() != null ? message.getMediaMetadata().getDownloadUrl() : null;

        if (sourceUrl == null || sourceUrl.isEmpty()) {
            Toast.makeText(this, "Audio resource reference path unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isLocalFile = sourceUrl.startsWith("content://") ||
                sourceUrl.startsWith("file://") ||
                sourceUrl.startsWith("/");

        if (!isLocalFile && !message.getSenderId().equals(currentUserId)) {
            holder.downloadContainer.setVisibility(View.VISIBLE);
            holder.downloadProgress.setIndeterminate(true);
            holder.downloadPercent.setText("Downloading...");

            MediaDownloadEngine downloader = new MediaDownloadEngine(this);
            String uniqueName = "REC_AUDIO_" + message.getClientMessageId() + ".m4a";

            downloader.downloadFileAsync(sourceUrl, uniqueName, new MediaDownloadEngine.DownloadCallback() {
                @Override
                public void onSuccess(Uri localFileUri) {
                    messageRepository.updateUploadedMediaUrl(
                            message.getClientMessageId(),
                            localFileUri.toString(),
                            message.getStatus()
                    );

                    holder.downloadContainer.setVisibility(View.GONE);
                    Toast.makeText(ChatMessageActivity.this, "Voice note saved locally.", Toast.LENGTH_SHORT).show();

                    if (message.getMediaMetadata() == null) {
                        message.setMediaMetadata(new MediaMetadata());
                    }
                    message.getMediaMetadata().setDownloadUrl(localFileUri.toString());
                    handleAudioPlaybackPipeline(message, holder);
                }

                @Override
                public void onFailure(Exception e) {
                    holder.downloadContainer.setVisibility(View.GONE);
                    Toast.makeText(ChatMessageActivity.this, "Failed to download voice note asset.", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        if (mediaPlayer != null && currentPlayingMessage != null &&
                currentPlayingMessage.getClientMessageId().equals(message.getClientMessageId())) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                holder.btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            } else {
                mediaPlayer.start();
                holder.btnPlayPause.setImageResource(R.drawable.ic_pause);
                runPlaybackTrackingLoop();
            }
            return;
        }

        stopAudioPlaybackEngine();

        activeAudioHolder = holder;
        currentPlayingMessage = message;

        try {
            mediaPlayer = new MediaPlayer();
            if (isLocalFile) {
                mediaPlayer.setDataSource(this, Uri.parse(sourceUrl));
            } else {
                if (sourceUrl.contains("localhost")) {
                    sourceUrl = sourceUrl.replace("localhost", "192.168.43.248");
                }
                mediaPlayer.setDataSource(sourceUrl);
            }

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                holder.downloadContainer.setVisibility(View.GONE);
                holder.btnPlayPause.setImageResource(R.drawable.ic_pause);
                holder.audioSeekBar.setMax(mp.getDuration());
                mp.start();
                runPlaybackTrackingLoop();
            });

            mediaPlayer.setOnCompletionListener(mp -> stopAudioPlaybackEngine());

        } catch (Exception e) {
            holder.downloadContainer.setVisibility(View.GONE);
            stopAudioPlaybackEngine();
        }
    }

    private void runPlaybackTrackingLoop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying() && activeAudioHolder != null) {
            int currentPos = mediaPlayer.getCurrentPosition();
            activeAudioHolder.audioSeekBar.setProgress(currentPos);

            int seconds = (currentPos / 1000) % 60;
            int minutes = (currentPos / (1000 * 60)) % 60;
            activeAudioHolder.txtDuration.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));

            seekHandler.postDelayed(this::runPlaybackTrackingLoop, 250);
        }
    }

    private void stopAudioPlaybackEngine() {
        seekHandler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (activeAudioHolder != null) {
            String playableUrl = (currentPlayingMessage != null && currentPlayingMessage.getMediaMetadata() != null)
                ? currentPlayingMessage.getMediaMetadata().getDownloadUrl() : null;

            boolean isLocalFile = playableUrl != null && (
                    playableUrl.startsWith("content://") ||
                            playableUrl.startsWith("file://") ||
                            playableUrl.startsWith("/")
            );

            if (isLocalFile) {
                activeAudioHolder.btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            } else {
                activeAudioHolder.btnPlayPause.setImageResource(R.drawable.ic_download);
            }

            activeAudioHolder.audioSeekBar.setProgress(0);
            activeAudioHolder.downloadContainer.setVisibility(View.GONE);
            activeAudioHolder = null;
        }
        currentPlayingMessage = null;
    }

    private void setupPermissionContracts() {
        audioPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Microphone access granted! Hold down to record.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Microphone access is required for voice notes.", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void setupResultContracts() {
        mediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (Exception e) {
                            Log.e("Chat", "Permission mapping crash", e);
                        }

                        MediaUploadTask task = new MediaUploadTask(uri, null);
                        processAndSendMedia(task);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String uriString = result.getData().getStringExtra("image_uri");
                        if (uriString != null) {
                            MediaUploadTask task = new MediaUploadTask(Uri.parse(uriString), null);
                            processAndSendMedia(task);
                        }
                    }
                }
        );
    }

    private void setupObservers() {
        messageRepository.getMessagesForConversation(activeChatRoomId).observe(this, newMessages -> {
            if (newMessages != null) {
                chatAdapter.submitList(newMessages, () -> {
                    if (!newMessages.isEmpty()) {
                        messageRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                });
            }
        });
    }

    private void setupInputListeners() {
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
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
                sendTextMessage(text);
            }
        });
    }

    private void setupMediaClickListeners() {
        btnAttachment.setOnClickListener(v -> mediaPickerLauncher.launch(new String[]{"image/*"}));
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(ChatMessageActivity.this, CameraActivity.class);
            cameraLauncher.launch(intent);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupAudioTouchPipeline() {
        btnMic.setOnClickListener(v -> {
            if (hasMicrophonePermission()) {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        btnMic.setOnTouchListener((v, event) -> {
            if (hasMicrophonePermission()) {
                return false;
            }

            long currentTime = System.currentTimeMillis();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (currentTime - lastClickTime < DEBOUNCE_DELAY_MS) {
                        return true;
                    }
                    lastClickTime = currentTime;
                    micStartY = event.getRawY();
                    executeAudioRecordStart();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (!audioRecorderEngine.isRecording()) return true;
                    float trackDiffY = micStartY - event.getRawY();

                    if (trackDiffY > 250) {
                        executeAudioRecordStop(false);
                        Toast.makeText(this, "Recording Cancelled", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (audioRecorderEngine.isRecording() || isRecordingInProgress) {
                        executeAudioRecordStop(true);
                    }
                    return true;
            }
            return false;
        });
    }

    private boolean hasMicrophonePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED;
    }

    private void executeAudioRecordStart() {
        if (isRecordingInProgress) return;

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            activeAudioFile = File.createTempFile("AUDIO_" + timeStamp + "_", ".m4a", getExternalCacheDir());

            micBg.setVisibility(View.VISIBLE);
            micWrapper.animate().translationY(-150f).setDuration(200).start();

            isRecordingInProgress = true;
            recordingStartTime = System.currentTimeMillis();
            audioRecorderEngine.startRecording(activeAudioFile);

        } catch (Exception e) {
            isRecordingInProgress = false;
            Log.e("ChatEnterprise", "Failed initiating secure cache storage tracking", e);
        }
    }

    private void executeAudioRecordStop(boolean commitToSend) {
        if (!isRecordingInProgress) return;

        micWrapper.animate().translationY(0f).setDuration(200).start();
        micBg.setVisibility(View.GONE);

        try {
            audioRecorderEngine.stopRecording();
        } catch (Exception e) {
            Log.e("ChatEnterprise", "Error stopping hardware recording core", e);
        }

        long calculatedDuration = System.currentTimeMillis() - recordingStartTime;
        isRecordingInProgress = false;

        if (commitToSend && calculatedDuration < MINIMUM_AUDIO_DURATION_MS) {
            Toast.makeText(this, "Voice note too short. Hold to record.", Toast.LENGTH_SHORT).show();
            commitToSend = false;
        }

        if (commitToSend && activeAudioFile != null && activeAudioFile.exists()) {
            long preciseDurationMs = extractTrueAudioDuration(activeAudioFile.getAbsolutePath(), calculatedDuration);

            Uri audioContentUri = Uri.fromFile(activeAudioFile);

            MediaMetadata audioSpecs = new MediaMetadata();
            audioSpecs.setDurationSeconds((int) (preciseDurationMs / 1000));

            MediaUploadTask task = new MediaUploadTask(audioContentUri, audioSpecs);
            processAndSendMedia(task);
        } else {
            cleanupTemporaryAudioFile();
        }
    }

    private long extractTrueAudioDuration(String filePath, long fallbackDuration) {
        android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            String timeStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (timeStr != null) {
                return Long.parseLong(timeStr);
            }
        } catch (Exception e) {
            Log.e("ChatEnterprise", "Failed parsing hardware audio header metadata", e);
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {}
        }
        return fallbackDuration;
    }

    private void cleanupTemporaryAudioFile() {
        if (activeAudioFile != null && activeAudioFile.exists()) {
            boolean deleted = activeAudioFile.delete();
            if(!deleted) Log.w("ChatEnterprise", "Stale temporary file could not be purged");
        }
    }

    private void sendTextMessage(String text) {
        long transientMessageId = System.currentTimeMillis();
        String clientMsgId = "client_msg_" + UUID.randomUUID().toString();

        ChatMessage newTextMessage = new ChatMessage(
                transientMessageId,
                activeChatRoomId,
                currentUserId,
                clientMsgId,
                System.currentTimeMillis(),
                ChatMessage.ContentType.TEXT,
                text,
                null,
                ChatMessage.MessageStatus.SENT
        );

        messageRepository.insertMessage(newTextMessage);
        messageInput.setText("");

        MessagePacket structuralPacket = MessageMapper.toPacket(newTextMessage);

        if (webSocketManager != null && structuralPacket != null) {
            webSocketManager.sendMessage(new Gson().toJson(structuralPacket));
        }
    }

    private void processAndSendMedia(MediaUploadTask task) {
        mediaUploadManager.processMediaMessageSend(task, activeChatRoomId, currentUserId, longLivedToken);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActiveConversationManager.getInstance().setActiveConversation(activeChatRoomId);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (activeAudioHolder != null) {
                activeAudioHolder.btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            }
        }
        ActiveConversationManager.getInstance().clearActiveConversation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioPlaybackEngine();
        if (audioRecorderEngine != null) {
            audioRecorderEngine.cancelRecording();
        }
    }
}
