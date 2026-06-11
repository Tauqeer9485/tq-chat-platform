package com.chat.server;

import com.chat.server.protocol.base.BasePacket;
import com.chat.server.protocol.chat.MessagePacket;
import com.chat.server.protocol.dispatcher.PacketDispatcher;
import com.chat.server.protocol.handler.MessagePacketHandler;
import com.chat.server.protocol.handler.PingPacketHandler;
import com.chat.server.protocol.type.PacketType;
import com.chat.server.security.JwtTokenProvider;
import com.chat.server.service.MessageService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.logging.Logger;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

import org.springframework.stereotype.Component;

/**
 * ChatServer is the main class that initializes and starts the Netty WebSocket server.
 * It registers packet handlers, sets up JWT authentication, and manages the server lifecycle.
 * The server listens for WebSocket connections on a specified port and processes incoming packets.
 */
@Component
public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());
    private static final int PORT = 8080;

    private final JwtTokenProvider jwtTokenProvider;
    private final MessageService messageService;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public ChatServer(JwtTokenProvider jwtTokenProvider, MessageService messageService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.messageService = messageService;
    }

    /**
     * Start Netty automatically in a background thread after the bean is constructed
     */
    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                startNettyServer();
            } catch (Exception e) {
                logger.severe("Failed to start Netty server: " + e.getMessage());
            }
        }, "netty-server-thread").start();
    }

    private void startNettyServer() throws Exception {
        PacketDispatcher dispatcher = new PacketDispatcher();

        dispatcher.registerHandler(PacketType.CHAT_MESSAGE, MessagePacket.class, new MessagePacketHandler(messageService));
        dispatcher.registerHandler(PacketType.PING, BasePacket.class, new PingPacketHandler());

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer(dispatcher, jwtTokenProvider))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            logger.info("Starting Netty WebSocket Server on port " + PORT + "...");
            ChannelFuture future = bootstrap.bind(PORT).sync();

            logger.info("Netty WebSocket Server started on port: " + PORT);
            logger.info("WebSocket URL: ws://localhost:" + PORT + "/ws");

            future.channel().closeFuture().sync();
        } finally {
            stop();
        }
    }

    /**
     * Ensures clean shutdown of Netty threads when your Spring app stops
     */
    @PreDestroy
    public void stop() {
        logger.info("Shutting down Netty server groups gracefully...");
        if (workerGroup != null) workerGroup.shutdownGracefully();
        if (bossGroup != null) bossGroup.shutdownGracefully();
    }
}
