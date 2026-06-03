package com.chat.server.service;

import com.chat.server.session.UserId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Service for managing conversations and their members.
 */
public class ConversationService {
    private static final Logger logger = Logger.getLogger(ConversationService.class.getName());
    private static ConversationService instance;
    
    private final Map<String, Set<UserId>> conversations = new ConcurrentHashMap<>();
    
    private ConversationService() {
    }
    
    /**
     * Get singleton instance of ConversationService
     * @return the singleton instance
     */
    public static synchronized ConversationService getInstance() {
        if (instance == null) {
            instance = new ConversationService();
        }
        return instance;
    }

    /**
     * Gets an existing direct conversation or creates it if it doesn't exist.
     *
     * @param user1 First participant
     * @param user2 Second participant
     * @return Conversation ID
     */
    public String getOrCreateDirectConversation(UserId user1, UserId user2) {
        if (user1.equals(user2)) {
            throw new IllegalArgumentException("Cannot create direct conversation with yourself");
        }

        String conversationId = createDirectConversationId(user1, user2);
        createConversation(conversationId, Set.of(user1, user2));

        return conversationId;
    }

    /**
     * Create conversation if not exists
     * @param conversationId the unique conversation identifier
     * @param members the set of member UserIds (UUID-based)
     */
    public void createConversation(String conversationId, Set<UserId> members) {
        if (conversationId == null || conversationId.isEmpty()) {
            logger.warning("Invalid conversationId provided to createConversation");
            return;
        }
        if (members == null || members.isEmpty()) {
            logger.warning("No members provided for conversation: " + conversationId);
            return;
        }
        
        conversations.computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet()).addAll(members);
        logger.info("Conversation created: " + conversationId + " with " + members.size() + " members");
    }

    /**
     * Add a member to a conversation
     * @param conversationId the conversation identifier
     * @param userId the UserId (UUID-based) to add
     */
    public void addMember(String conversationId, UserId userId) {
        if (conversationId == null || conversationId.isEmpty()) {
            logger.warning("Invalid conversationId provided to addMember");
            return;
        }

        boolean added = conversations.computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        if (added) {
            logger.info("User " + userId + " added to conversation " + conversationId);
        } else {
            logger.warning("User " + userId + " already in conversation " + conversationId);
        }
    }

    /**
     * Remove a member from a conversation
     * @param conversationId the conversation identifier
     * @param userId the UserId (UUID-based) to remove
     */
    public void removeMember(String conversationId, UserId userId) {
        Set<UserId> members = conversations.get(conversationId);
        if (members != null) {
            boolean removed = members.remove(userId);
            if (removed) {
                logger.info("User " + userId + " removed from conversation " + conversationId);
                if (members.isEmpty()) {
                    conversations.remove(conversationId);
                    logger.info("Conversation " + conversationId + " deleted (no members remaining)");
                }
            } else {
                logger.warning("User " + userId + " not found in conversation " + conversationId);
            }
        } else {
            logger.warning("Conversation " + conversationId + " not found");
        }
    }

    /**
     * Get all members of a conversation
     * @param conversationId the conversation identifier
     * @return an unmodifiable set of member UserIds (UUID-based)
     */
    public Set<UserId> getMembers(String conversationId) {
        Set<UserId> members = conversations.get(conversationId);
        return members != null ? Collections.unmodifiableSet(members) : Collections.emptySet();
    }

    /**
     * Check if a user is a member of a conversation
     * @param conversationId the conversation identifier
     * @param userId the UserId (UUID-based) to check
     * @return true if user is a member, false otherwise
     */
    public boolean isMember(String conversationId, UserId userId) {
        Set<UserId> members = conversations.get(conversationId);
        return members != null && members.contains(userId);
    }

    /**
     * Generate direct message conversation ID 
     * @param user1 first UserId (UUID-based)
     * @param user2 second UserId (UUID-based)
     * @return consistent direct message conversation ID
     */

    public String createDirectConversationId(UserId user1, UserId user2) {
        String uuid1 = user1.asString();
        String uuid2 = user2.asString();
        
        int comparison = uuid1.compareTo(uuid2);
        if (comparison < 0) {
            return "dm_" + uuid1 + "_" + uuid2;
        } else {
            return "dm_" + uuid2 + "_" + uuid1;
        }
    }
    
    /**
     * Delete a conversation completely
     * @param conversationId the conversation to delete
     * @return true if conversation was deleted, false if not found
     */
    public boolean deleteConversation(String conversationId) {
        boolean deleted = conversations.remove(conversationId) != null;
        if (deleted) {
            logger.info("Conversation " + conversationId + " deleted");
        }
        return deleted;
    }
    
    /**
     * Get all conversations
     * @return number of active conversations
     */
    public int getConversationCount() {
        return conversations.size();
    }
    
    /**
     * Clear all conversations (use with caution)
     */
    public void clearAllConversations() {
        conversations.clear();
        logger.warning("All conversations cleared");
    }
}