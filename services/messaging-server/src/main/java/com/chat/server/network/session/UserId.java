package com.chat.server.network.session;

import java.util.UUID;
import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Value object representing a user identifier.
 * Uses UUID for distributed systems, E2EE chat applications, and improved security.
 */
@JsonSerialize(using = UserIdSerializer.class)
@JsonDeserialize(using = UserIdDeserializer.class)
public class UserId implements Serializable, Comparable<UserId> {
    private static final long serialVersionUID = 1L;
    
    private final UUID id;
    
    /**
     * Create a UserId from a UUID
     * @param id the UUID
     */
    public UserId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        this.id = id;
    }
    
    /**
     * Create a UserId from a UUID string
     * @param uuidString the UUID as string (e.g., "550e8400-e29b-41d4-a716-446655440000")
     */
    public UserId(String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            throw new IllegalArgumentException("UserId string cannot be null or empty");
        }
        try {
            this.id = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }
    }
    
    /**
     * Generate a new random UserId
     * @return a new UserId with random UUID
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
    
    /**
     * Get the UUID
     * @return the underlying UUID
     */
    public UUID getUUID() {
        return id;
    }
    
    /**
     * Get the UserId as string
     * @return UUID as string
     */
    public String asString() {
        return id.toString();
    }
    
    @Override
    public String toString() {
        return id.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserId userId = (UserId) obj;
        return id.equals(userId.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public int compareTo(UserId other) {
        if (other == null) return 1;
        return this.id.compareTo(other.id);
    }
}
