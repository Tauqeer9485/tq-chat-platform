package com.chat.server.conversation;

import com.chat.server.user.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

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