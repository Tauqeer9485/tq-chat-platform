package com.chat.server.handler.packet;

import com.chat.server.handler.PacketHandler;
import com.chat.server.packet.base.BasePacket;
import com.chat.server.packet.type.PacketType;

import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Handler for PING packets.
 * Responds with PONG packet via context (connection handler level).
 * Note: At this handler level, session context is not available,
 * so we respond at the channel level.
 */

public class PingPacketHandler implements PacketHandler<BasePacket> {
    private static final Logger logger = Logger.getLogger(PingPacketHandler.class.getName());

    @Override
    public void handle(ChannelHandlerContext ctx, BasePacket packet) {
        logger.info("PING received");

        BasePacket pongPacket = new BasePacket(PacketType.PONG);
        String json = com.chat.server.protocol.PacketSerializer.toJson(pongPacket);
        
        ctx.writeAndFlush(new TextWebSocketFrame(json));
    }
}