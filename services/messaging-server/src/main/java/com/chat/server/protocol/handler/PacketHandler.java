package com.chat.server.protocol.handler;

import io.netty.channel.ChannelHandlerContext;

public interface PacketHandler<T> {
    void handle(ChannelHandlerContext ctx, T packet) throws Exception;
}