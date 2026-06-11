package com.chat.server.network.handler;

import com.chat.server.protocol.dispatcher.PacketDispatcher;
import com.chat.server.util.ChannelAttributes;

import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;

public class ServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger logger = Logger.getLogger(ServerHandler.class.getName());

    private final PacketDispatcher dispatcher;

    public ServerHandler(PacketDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String username = ctx.channel().attr(ChannelAttributes.USERNAME).get();

        if (username != null) {
            logger.info("✓ [CHANNEL] Active: " + username);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {        
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;

            String msg = textFrame.text();            
            String username = ctx.channel().attr(ChannelAttributes.USERNAME).get();
            
            logger.info("[MESSAGE] From " + username + ": " + msg);
            
            dispatcher.dispatch(ctx, msg);
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String username = ctx.channel().attr(ChannelAttributes.USERNAME).get();
        
        if (username != null) {
            logger.info("✓ [CHANNEL] Inactive: " + username);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.severe("Error: " + cause.getMessage());
        ctx.close();
    }
}
