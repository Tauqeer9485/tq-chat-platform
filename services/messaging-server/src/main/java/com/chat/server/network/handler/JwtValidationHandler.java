package com.chat.server.network.handler;

import com.chat.server.security.JwtTokenProvider;
import com.chat.server.util.ChannelAttributes;

import java.util.List;
import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;


/**
 * JWT Validation Handler
 * 
 * Validates JWT token from WebSocket upgrade request query parameters
 * BEFORE the WebSocket handshake is initiated.
 * 
 * This handler rejects invalid tokens at the HTTP level, preventing
 * the WebSocket handshake from starting if the token is invalid.
 * 
 * Handles both HttpRequest and FullHttpRequest (from HttpObjectAggregator).
 */
public class JwtValidationHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(JwtValidationHandler.class.getName());
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtValidationHandler(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (!(msg instanceof HttpRequest)) {
                ctx.fireChannelRead(msg);
                return;
            }

            HttpRequest request = (HttpRequest) msg;

            Boolean isValidated = ctx.channel().attr(ChannelAttributes.JWT_VALIDATED).get();
            if (isValidated == null) {
                isValidated = false;
            }

            if (!isValidated && request != null && isWebSocketUpgradeRequest(request)) {

                String authHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
                String token = JwtTokenProvider.extractTokenFromAuthHeader(authHeader);

                // String uri = request.uri();
                // String token = JwtUtil.extractTokenFromUri(uri);

                logger.info("[JWT-VALIDATION] WebSocket upgrade attempt: " + authHeader + " / " + request.uri());

                if (token == null || token.isBlank()) {
                    logger.warning("[JWT-VALIDATION] Rejected: No JWT token provided");
                    sendUnauthorizedResponse(ctx);
                    return;
                }

                if (!jwtTokenProvider.validateToken(token)) {
                    logger.warning("[JWT-VALIDATION] Rejected: Invalid JWT token");
                    sendUnauthorizedResponse(ctx);
                    return;
                }

                String userId = jwtTokenProvider.getUserIdFromToken(token);
                String username = jwtTokenProvider.getUsernameFromToken(token);
                List<String> scopes = jwtTokenProvider.getScopesFromToken(token);

                if (userId == null || username == null || scopes == null) {
                    logger.warning("[JWT-VALIDATION] Rejected: Could not extract userId/username from token");
                    sendUnauthorizedResponse(ctx);
                    return;
                }

                if (!scopes.contains("websocket:connect")) {
                    logger.warning("[JWT-VALIDATION] Rejected: Token does not have websocket:connect scope");
                    sendUnauthorizedResponse(ctx);
                    return;
                }

                ctx.channel().attr(ChannelAttributes.USER_ID).set(userId);
                ctx.channel().attr(ChannelAttributes.USERNAME).set(username);
                ctx.channel().attr(ChannelAttributes.JWT_VALIDATED).set(true);

                logger.info("[JWT-VALIDATION] Accepted: " + username + " (" + userId + ")");
            }

            ctx.fireChannelRead(msg);

        } catch (Exception e) {
            logger.severe("[JWT-VALIDATION] Error: " + e.getMessage());
            e.printStackTrace();

            sendUnauthorizedResponse(ctx);
        }
    }

    /**
     * Check if this is a WebSocket upgrade request
     */
    private boolean isWebSocketUpgradeRequest(HttpRequest request) {
        String upgrade = request.headers().get(HttpHeaderNames.UPGRADE);
        String connection = request.headers().get(HttpHeaderNames.CONNECTION);
        
        boolean isUpgrade = upgrade != null && upgrade.equalsIgnoreCase("websocket");
        boolean isConnection = connection != null && connection.toUpperCase().contains("UPGRADE");
        
        return isUpgrade && isConnection;
    }

    /**
     * Send 401 Unauthorized response for invalid tokens
     */
    private void sendUnauthorizedResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.UNAUTHORIZED
        );
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        response.headers().set(HttpHeaderNames.CONNECTION, "close");
        ctx.writeAndFlush(response).addListener(future -> ctx.close());
    }
}
