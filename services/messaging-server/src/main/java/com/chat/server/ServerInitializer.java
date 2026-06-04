package com.chat.server;

import com.chat.server.dispatcher.PacketDispatcher;
import com.chat.server.handler.HttpRequestCaptureHandler;
import com.chat.server.handler.JwtValidationHandler;
import com.chat.server.handler.ServerHandler;
import com.chat.server.handler.WebSocketAuthHandler;
import com.chat.server.security.JwtTokenProvider;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * Netty Channel Pipeline Initializer
 * Pipeline order:
 * 1. HttpServerCodec                - Encodes/decodes HTTP requests/responses
 * 2. HttpObjectAggregator           - Aggregates HTTP fragments
 * 3. HttpRequestCaptureHandler      - Captures request URI before handshake
 * 4. JwtValidationHandler           - VALIDATES JWT BEFORE WebSocket upgrade (CRITICAL FIX)
 * 5. WebSocketServerProtocolHandler - Only reached if JWT is valid
 * 6. WebSocketAuthHandler           - Session management after successful upgrade
 * 7. ServerHandler                  - Processes WebSocket messages
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final PacketDispatcher dispatcher;
    private final JwtTokenProvider jwtTokenProvider;

    public ServerInitializer(PacketDispatcher dispatcher, JwtTokenProvider jwtTokenProvider) {
        this.dispatcher = dispatcher;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void initChannel(SocketChannel ch) {

        ChannelPipeline pipeline = ch.pipeline();

        // HTTP codec handlers
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        
        // Aggregate HTTP fragments
        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(262144)); // 256KB buffer
        
        // Capture request URI (with JWT token in query params)
        pipeline.addLast("httpRequestCapture", new HttpRequestCaptureHandler());
        
        // VALIDATE JWT BEFORE WebSocket upgrade (BEFORE WebSocketServerProtocolHandler)
        pipeline.addLast("jwtValidation", new JwtValidationHandler(jwtTokenProvider));
        
        // WebSocket protocol handler (only reached if JWT valid)
        pipeline.addLast("webSocketProtocol", 
            new WebSocketServerProtocolHandler("/ws", null, true, 65536, true, true, 10000L));
        
        // Session management and user tracking after WebSocket connection established
        pipeline.addLast("webSocketAuth", new WebSocketAuthHandler());
        
        // Business logic handler
        pipeline.addLast("serverHandler", new ServerHandler(dispatcher));
    }
}