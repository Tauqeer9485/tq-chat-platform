package com.chat.server;

import com.chat.server.auth.JwtTokenProvider;
import com.chat.server.dispatcher.PacketDispatcher;
import com.chat.server.handler.packet.MessagePacketHandler;
import com.chat.server.handler.packet.PingPacketHandler;
import com.chat.server.packet.type.PacketType;
import com.chat.server.packet.base.BasePacket;
import com.chat.server.packet.chat.MessagePacket;

import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Netty WebSocket Server for real-time messaging
 * Now started from ChatApplication (Spring Boot)
 * Validates JWT tokens on WebSocket connections
 */
public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private static final int PORT = 8080;

    /**
     * Start Netty WebSocket Server with JWT validation
     * Called from ChatApplication after Spring Boot starts
     * 
     * @param jwtTokenProvider Spring bean for JWT validation
     */
    public static void startNettyServer(JwtTokenProvider jwtTokenProvider) throws Exception {
        PacketDispatcher dispatcher = new PacketDispatcher();

        dispatcher.registerHandler(PacketType.CHAT_MESSAGE, MessagePacket.class, new MessagePacketHandler());
        dispatcher.registerHandler(PacketType.PING, BasePacket.class, new PingPacketHandler());

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer(dispatcher, jwtTokenProvider))
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            logger.info(" Starting Netty WebSocket Server on port " + PORT + "...");

            ChannelFuture future = bootstrap.bind(PORT).sync();

            logger.info(" Netty WebSocket Server started on port: " + PORT);
            logger.info(" WebSocket URL: ws://localhost:" + PORT + "/ws");

            future.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}