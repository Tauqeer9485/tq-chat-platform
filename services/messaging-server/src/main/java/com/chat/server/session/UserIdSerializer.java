package com.chat.server.session;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * Jackson serializer for UserId - converts UserId to JSON string
 * Example: UserId -> "550e8400-e29b-41d4-a716-446655440000"
 */
public class UserIdSerializer extends JsonSerializer<UserId> {

    @Override
    public void serialize(UserId userId, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (userId == null) {
            gen.writeNull();
        } else {
            gen.writeString(userId.asString());
        }
    }
}
