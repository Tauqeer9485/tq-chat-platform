package com.chat.server.handler;

import com.chat.server.util.ChannelAttributes;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Captures the request URI before WebSocket upgrade
 * 
 * This handler stores the full request URI (including query parameters)
 * in channel attributes so that WebSocketAuthHandler can access it later.
 */
public class HttpRequestCaptureHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            ctx.channel().attr(ChannelAttributes.REQUEST_URI).set(request.uri());
        } else if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            ctx.channel().attr(ChannelAttributes.REQUEST_URI).set(request.uri());
        }

        // Pass all messages through
        ctx.fireChannelRead(msg);
    }
}
