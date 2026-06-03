package com.chat.server.util;

import io.netty.util.AttributeKey;

/**
 * Centralized Netty channel attribute keys
 * Prevents attribute name mismatches between handlers
 */
public class ChannelAttributes {
    
    public static final AttributeKey<String> REQUEST_URI = AttributeKey.valueOf("request_uri");
    public static final AttributeKey<String> USER_ID = AttributeKey.valueOf("user_id");
    public static final AttributeKey<String> USERNAME = AttributeKey.valueOf("username");
    public static final AttributeKey<Boolean> JWT_VALIDATED = AttributeKey.valueOf("jwt_validated");
    
    private ChannelAttributes() {
        // Private constructor to prevent instantiation
    }
}
