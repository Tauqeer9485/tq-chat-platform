package com.tq.distributed_chat_android.data.protocol.handler;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.tq.distributed_chat_android.data.mapper.MessageMapper;
import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.protocol.chat.MessagePacket;
import com.tq.distributed_chat_android.data.repository.ConversationRepository;
import com.tq.distributed_chat_android.data.repository.MessageRepository;
import com.tq.distributed_chat_android.util.ActiveConversationManager;
import com.tq.distributed_chat_android.util.MediaDownloadEngine;

public class MessagePacketHandler implements PacketHandler<MessagePacket> {
    private static final String TAG = "MessagePacketHandler";
    private final Context context;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;


    public MessagePacketHandler(Context context) {
        this.context = context.getApplicationContext();
        this.messageRepository = MessageRepository.getInstance(context.getApplicationContext());
        this.conversationRepository = ConversationRepository.getInstance(context.getApplicationContext());
    }

    @Override
    public void handle(MessagePacket packet) {
        if (packet == null) return;

        try {
            Log.d(TAG, "Processing MessagePacket for conversation: " + packet.getConversationId());

            ChatMessage domainMessage = MessageMapper.mapToDomain(packet);
            if (domainMessage == null) return;

            if (domainMessage.getContentType() == ChatMessage.ContentType.AUDIO) {

                if (ActiveConversationManager.getInstance().isActive(domainMessage.getConversationId())) {
                    Log.d(TAG, "Active chat window detected. Initiating immediate auto-download pipeline.");

                    MediaDownloadEngine downloader = new MediaDownloadEngine(context);
                    String localFileName = "REC_AUDIO_" + domainMessage.getClientMessageId() + ".m4a";

                    downloader.downloadFileAsync(domainMessage.getMediaUrl(), localFileName, new MediaDownloadEngine.DownloadCallback() {
                        @Override
                        public void onSuccess(Uri localFileUri) {
                            Log.d(TAG, "Auto-download complete. Swapping network link with local file URI path.");

                            domainMessage.setMediaUrl(localFileUri.toString());

                            saveMessageAndNotifyConversation(domainMessage);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Auto-download failed mid-transit. Saving remote link as fallback.", e);
                            saveMessageAndNotifyConversation(domainMessage);
                        }
                    });
                    return;
                }
            }

            saveMessageAndNotifyConversation(domainMessage);
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle message packet", e);
        }
    }

    private void saveMessageAndNotifyConversation(ChatMessage message) {
        messageRepository.insertMessage(message);
        conversationRepository.updateLastMessage(
                message.getConversationId(),
                message.getContent(),
                message.getTimestamp()
        );
    }
}