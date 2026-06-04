package com.chat.server.conversation;

import com.chat.server.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * ConversationMember entity representing the association between a user and a conversation
 * Stores information about when a user joined a conversation
 */
@Entity
@Table(
        name = "conversation_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"conversation_id", "user_id"}
                )
        }
)
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Long joinedAt;

    public ConversationMember() {
        this.joinedAt = System.currentTimeMillis();
    }

    public ConversationMember(
            Conversation conversation,
            User user
    ) {
        this.conversation = conversation;
        this.user = user;
        this.joinedAt = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public User getUser() {
        return user;
    }

    public Long getJoinedAt() {
        return joinedAt;
    }
}