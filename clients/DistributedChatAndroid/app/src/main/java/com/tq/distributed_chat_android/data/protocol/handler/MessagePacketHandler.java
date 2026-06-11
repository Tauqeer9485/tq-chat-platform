package com.tq.distributed_chat_android.data.protocol.handler;

import android.content.Context;
import android.util.Log;

import com.tq.distributed_chat_android.data.mapper.MessageMapper;
import com.tq.distributed_chat_android.data.model.ChatMessage;
import com.tq.distributed_chat_android.data.protocol.chat.MessagePacket;
import com.tq.distributed_chat_android.data.repository.MessageRepository;

public class MessagePacketHandler implements PacketHandler<MessagePacket> {
    private static final String TAG = "MessagePacketHandler";
    private final MessageRepository messageRepository;

    public MessagePacketHandler(Context context) {
        this.messageRepository = MessageRepository.getInstance(context.getApplicationContext());
    }

    @Override
    public void handle(MessagePacket packet) {
        if (packet == null) return;

        try {
            Log.d(TAG, "Processing MessagePacket for conversation: " + packet.getConversationId());

            ChatMessage domainMessage = MessageMapper.mapToDomain(packet);

            if (domainMessage != null) {
                messageRepository.insertMessage(domainMessage);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to handle message packet", e);
        }
    }
}