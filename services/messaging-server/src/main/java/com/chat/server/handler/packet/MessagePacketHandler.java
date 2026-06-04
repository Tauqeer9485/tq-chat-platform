package com.chat.server.handler.packet;

import com.chat.server.handler.PacketHandler;
import com.chat.server.packet.chat.MessagePacket;
import com.chat.server.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;

public class MessagePacketHandler implements PacketHandler<MessagePacket> {
    private static final Logger logger = Logger.getLogger(MessagePacketHandler.class.getName());

    private final MessageService messageService;
    
    public MessagePacketHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, MessagePacket packet) throws NumberFormatException, JsonProcessingException {

        logger.info("Message from " + packet.getSenderId() + ": " + packet.getContent());

        messageService.processMessage(packet);
    }
}   