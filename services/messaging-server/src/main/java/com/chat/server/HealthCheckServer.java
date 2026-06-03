package com.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * Lightweight Health Check Server
 * 
 * Runs on a separate port (9090) to handle Docker health checks
 * This prevents health check requests from reaching the WebSocket server
 */
public class HealthCheckServer {
    
    private static final int HEALTH_CHECK_PORT = 9090;
    
    public static void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HealthCheckHandler());
                        }
                    });
            
            bootstrap.bind(HEALTH_CHECK_PORT).sync();
            System.out.println("✓ Health Check Server started on port: " + HEALTH_CHECK_PORT);
            System.out.println("  Health Check URL: http://localhost:" + HEALTH_CHECK_PORT + "/health");
            
        } catch (InterruptedException e) {
            System.err.println("Health Check Server startup interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            // Note: Not calling shutdownGracefully() here as the server should run indefinitely
        }
    }
    
    /**
     * Handler for health check requests
     */
    private static class HealthCheckHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                String path = request.uri();
                
                if (path.startsWith("/health")) {
                    String response = "{\"status\":\"UP\"}";
                    FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(response, CharsetUtil.UTF_8)
                    );
                    
                    httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                    httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.length());
                    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                    
                    ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
                    return;
                }
                
                String response = "Not Found";
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.NOT_FOUND,
                    Unpooled.copiedBuffer(response, CharsetUtil.UTF_8)
                );
                httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.length());
                httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                
                ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}
