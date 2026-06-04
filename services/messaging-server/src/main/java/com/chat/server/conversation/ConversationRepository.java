package com.chat.server.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Conversation entity
 * Provides database operations for conversations
 */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByConversationId(String conversationId);

    boolean existsByConversationId(String conversationId);
}