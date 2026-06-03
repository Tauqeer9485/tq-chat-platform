package com.chat.server.handler;

import io.netty.channel.ChannelHandlerContext;

public interface PacketHandler<T> {
    void handle(ChannelHandlerContext ctx, T packet) throws Exception;
}