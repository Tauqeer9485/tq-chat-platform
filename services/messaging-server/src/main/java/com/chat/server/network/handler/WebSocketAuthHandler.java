package com.chat.server.network.handler;

import com.chat.server.network.session.Session;
import com.chat.server.network.session.SessionManager;
import com.chat.server.network.session.UserId;
import com.chat.server.util.ChannelAttributes;

import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * WebSocket Session Management
 * 
 * Creates a session and keeps connection open.
 */
public class WebSocketAuthHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(WebSocketAuthHandler.class.getName());

    
    //private final JwtTokenProvider jwtTokenProvider;
    
    public WebSocketAuthHandler() {
    }       
    
    // public WebSocketAuthHandler(JwtTokenProvider jwtTokenProvider) {
    //     this.jwtTokenProvider = jwtTokenProvider;
    // }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        
        if (evt instanceof WebSocketServerProtocolHandler.ServerHandshakeStateEvent) {
            WebSocketServerProtocolHandler.ServerHandshakeStateEvent handshakeEvent = (WebSocketServerProtocolHandler.ServerHandshakeStateEvent) evt;
            
            // WebSocket handshake completed - now validate JWT
            String eventString = handshakeEvent.toString();
        
            if (eventString.contains("HANDSHAKE_COMPLETE")) {
                handleWebSocketUpgrade(ctx);
            } else if (eventString.contains("HANDSHAKE_FAILURE")) {
                logger.warning("❌ [AUTH] WebSocket handshake failed");
                ctx.close();
            }
            
            // if (handshakeEvent.toString().contains("HANDSHAKE_COMPLETE")) {
            //     handleWebSocketUpgrade(ctx);
            // }
        }
        
        ctx.fireUserEventTriggered(evt);
    }

    private void handleWebSocketUpgrade(ChannelHandlerContext ctx) {
        Boolean validated = ctx.channel().attr(ChannelAttributes.JWT_VALIDATED).get();

        if (!Boolean.TRUE.equals(validated)) {
            logger.warning("[AUTH] JWT validation missing");
            ctx.close();
            return;
        }

        String userId = ctx.channel().attr(ChannelAttributes.USER_ID).get();
        String username = ctx.channel().attr(ChannelAttributes.USERNAME).get();

        if (userId == null || username == null) {
            logger.warning("❌ [AUTH] Missing authenticated user data");
            ctx.close();
            return;
        }

        UserId userIdObj = new UserId(userId);

        Session session = new Session(userIdObj, username, ctx.channel());
        SessionManager.addSession(session);

        logger.info("✓ [AUTH] WebSocket authenticated: " + username + " (" + userId + ")"); 
        logger.info("✓ [CHANNEL] Active: " + username);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Clean up session when connection closes
        String userIdStr = ctx.channel().attr(ChannelAttributes.USER_ID).get();
        String username = ctx.channel().attr(ChannelAttributes.USERNAME).get();
        
        if (userIdStr != null) {
            SessionManager.removeSession(new UserId(userIdStr));
            logger.info("✓ [CHANNEL] Inactive: " + username);
        }
        
        ctx.fireChannelInactive();
    }
}

