package com.chat.server.util;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Utility for JWT token extraction from URIs
 * Single source of truth for token extraction logic
 */
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String TOKEN_PARAM_NAME = "token";

    /**
     * Extract JWT token from WebSocket URI query parameters
     * Expected format: /ws?token=eyJhbGc...
     * 
     * @param uri The request URI containing query parameters
     * @return The token value, or null if not found
     */
    public static String extractTokenFromUri(String uri) {
        try {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(uri);
            Map<String, List<String>> params = queryDecoder.parameters();
            
            if (params.containsKey(TOKEN_PARAM_NAME)) {
                List<String> tokens = params.get(TOKEN_PARAM_NAME);
                if (!tokens.isEmpty()) {
                    String token = tokens.get(0);
                    if (token != null && !token.isEmpty()) {
                        return token;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error parsing URI for token: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extract JWT token from Authorization header
     * Expected format: "Bearer eyJhbGc..."
     * 
     * @param authHeader The Authorization header value
     * @return The token value, or null if not in correct format
     */
    public static String extractTokenFromAuthHeader(String authHeader) {
        if (authHeader == null) {
            return null;
        }

        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7).trim();

        return token.isEmpty() ? null : token;
    }
    
    private JwtUtil() {
        // private constructor
    }
}
