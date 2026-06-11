package com.chat.server.network.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * HTTP Health Check Handler
 * 
 * Handles non-WebSocket HTTP requests like /health, /status, etc.
 * Responds with 200 OK and passes through WebSocket upgrade requests.
 * 
 * MUST be placed BEFORE JwtValidationHandler in the pipeline.
 */
public class HttpHealthHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            
            if (isWebSocketUpgradeRequest(request)) {
                ctx.fireChannelRead(msg);
                return;
            }
            
            String path = request.uri();
            
            if (path.equals("/health") || path.equals("/health/live") || path.equals("/health/ready")) {
                handleHealthCheck(ctx);
                return;
            }
            
            sendHttpResponse(ctx, HttpResponseStatus.NOT_FOUND, "Endpoint not found");
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * Check if this is a WebSocket upgrade request
     */
    private boolean isWebSocketUpgradeRequest(HttpRequest request) {
        String upgrade = request.headers().get(HttpHeaderNames.UPGRADE);
        String connection = request.headers().get(HttpHeaderNames.CONNECTION);
        
        return upgrade != null && upgrade.equalsIgnoreCase("websocket") &&
               connection != null && connection.toUpperCase().contains("UPGRADE");
    }

    /**
     * Handle health check requests
     */
    private void handleHealthCheck(ChannelHandlerContext ctx) {
        String response = "{\"status\":\"UP\"}";
        
        FullHttpResponse fullResponse = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.copiedBuffer(response, CharsetUtil.UTF_8)
        );
        
        fullResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        fullResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.length());
        fullResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        
        ctx.writeAndFlush(fullResponse).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Send generic HTTP response
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String body) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            status,
            Unpooled.copiedBuffer(body, CharsetUtil.UTF_8)
        );
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, body.length());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
