package com.chat.server.core.conversation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.server.core.user.User;

/**
 * Repository for ConversationMember entity
 * Provides database operations for conversation members
 */
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    List<ConversationMember> findByConversation(Conversation conversation);
    List<ConversationMember> findByUser(User user);

    long countByConversation(Conversation conversation);
    
    boolean existsByConversationAndUser(Conversation conversation, User user);

    void deleteByConversationAndUser(Conversation conversation, User user);
}