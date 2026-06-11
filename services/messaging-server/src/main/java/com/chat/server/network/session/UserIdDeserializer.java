package com.chat.server.network.session;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 * Jackson deserializer for UserId - converts JSON string to UserId
 * Handles UUID string format from JSON
 * Example: "550e8400-e29b-41d4-a716-446655440000" -> UserId
 */
public class UserIdDeserializer extends JsonDeserializer<UserId> {

    @Override
    public UserId deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        String uuidString = parser.getValueAsString();
        
        if (uuidString == null || uuidString.isEmpty()) {
            return null;
        }
        
        try {
            return new UserId(uuidString);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid UUID format: " + uuidString, e);
        }
    }
}
