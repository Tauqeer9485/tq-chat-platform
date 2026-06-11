package com.chat.server.network.session;

import com.chat.server.protocol.serializer.PacketSerializer;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Represents a user session 
 */
public class Session {

    private final UserId userId;
    private final String username;
    private final Channel channel;

    public Session(
            UserId userId,
            String username,
            Channel channel
    ) {
        this.userId = userId;
        this.username = username;
        this.channel = channel;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Channel getChannel() {
        return channel;
    }

    public void sendPacket(Object packet) {
        String json = PacketSerializer.toJson(packet);
        
        channel.writeAndFlush(new TextWebSocketFrame(json));
    }
}