package com.chat.server.core.conversation;

import com.chat.server.core.user.User;
import com.chat.server.core.user.UserRepository;
import com.chat.server.network.session.UserId;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * Service for managing conversations and their members.
 */
@Service
public class ConversationService {
    private static final Logger logger = Logger.getLogger(ConversationService.class.getName());

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserRepository userRepository;

    public ConversationService(ConversationRepository conversationRepository, ConversationMemberRepository memberRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
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
            throw new IllegalArgumentException(
                    "Cannot create direct conversation with yourself"
            );
        }

        String conversationId = createDirectConversationId(user1, user2);

        Optional<Conversation> existing = conversationRepository.findByConversationId(conversationId);

        if (existing.isPresent()) {
            return conversationId;
        }

        Conversation conversation = new Conversation(conversationId, ConversationType.DIRECT);

        conversationRepository.save(conversation);

        addMember(conversationId, user1);
        addMember(conversationId, user2);

        logger.info("Created direct conversation: " + conversationId);

        return conversationId;
    }

    
    /**
     * Create conversation if not exists
     * @param conversationId the unique conversation identifier
     * @param members the set of member UserIds (UUID-based)
     */
    public void createConversation(String conversationId, Set<UserId> members) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId cannot be empty");
        }

        if (members == null ||members.isEmpty()) {
            throw new IllegalArgumentException("members cannot be empty");
        }

        if (conversationRepository.existsByConversationId(conversationId)) {
            return;
        }

        Conversation conversation = new Conversation(conversationId, ConversationType.GROUP);

        conversationRepository.save(conversation);

        for (UserId member : members) {
            addMember(conversationId, member);
        }

        logger.info("Created conversation: " + conversationId);
    }

    /**
     * Add a member to a conversation
     * @param conversationId the conversation identifier
     * @param userId the UserId (UUID-based) to add
     */
    public void addMember(String conversationId, UserId userId) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId).orElseThrow(() ->
            new IllegalArgumentException("Conversation not found: " + conversationId)
        );
        User user = userRepository.findByUserId(userId.asString()).orElseThrow(() ->
            new IllegalArgumentException("User not found: " + userId.asString())
        );

        if (memberRepository.existsByConversationAndUser(conversation, user)) {
            return;
        }

        memberRepository.save(new ConversationMember(conversation, user));

        logger.info("Added user " + userId.asString() + " to conversation " + conversationId);
    }

    /**
     * Remove a member from a conversation
     * @param conversationId the conversation identifier
     * @param userId the UserId (UUID-based) to remove
     */
    public void removeMember(String conversationId, UserId userId) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId).orElseThrow();
        User user = userRepository.findByUserId(userId.asString()).orElseThrow();

        memberRepository.deleteByConversationAndUser(conversation, user);

        long remainingMembers = memberRepository.findByConversation(conversation).size();

        if (remainingMembers == 0) {
            conversationRepository.delete(conversation);

            logger.info("Deleted empty conversation: " + conversationId);
        }
    }

    /**
     * Get all members of a conversation
     * @param conversationId the conversation identifier
     * @return an unmodifiable set of member UserIds (UUID-based)
     */
    public Set<UserId> getMembers(String conversationId) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId).orElseThrow();

        return memberRepository.findByConversation(conversation).stream().map(member ->
            new UserId(
                    member.getUser().getUserId()
            )
        ).collect(Collectors.toSet());
    }
    
    /**
     * Check if a user is a member of a conversation
     * @param conversationId the conversation identifier
     * @param userId the UserId (UUID-based) to check
     * @return true if user is a member, false otherwise
     */
    public boolean isMember(String conversationId, UserId userId) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId).orElseThrow();
        User user = userRepository.findByUserId(userId.asString()).orElseThrow();

        return memberRepository.existsByConversationAndUser(conversation, user);
    }

    /**
     * Delete a conversation completely
     * @param conversationId the conversation to delete
     * @return true if conversation was deleted, false if not found
     */
    public boolean deleteConversation(String conversationId) {
        Optional<Conversation> conversation = conversationRepository.findByConversationId(conversationId);

        if (conversation.isEmpty()) {
            return false;
        }

        Conversation conv = conversation.get();

        memberRepository.deleteAll(memberRepository.findByConversation(conv));
        conversationRepository.delete(conv);

        logger.info("Deleted conversation: " + conversationId);

        return true;
    }

    /**
     * Get all conversations
     * @return number of active conversations
     */
    public long getConversationCount() {
        return conversationRepository.count();
    }
    
    /**
     * Clear all conversations (use with caution)
     */
    public void clearAllConversations() {
        memberRepository.deleteAll();
        conversationRepository.deleteAll();

        logger.warning("All conversations deleted");
    }

        /**
     * Generate direct message conversation ID 
     * @param user1 first UserId (UUID-based)
     * @param user2 second UserId (UUID-based)
     * @return consistent direct message conversation ID
     */
    private String createDirectConversationId(UserId user1, UserId user2) {
        String id1 = user1.asString();
        String id2 = user2.asString();

        return id1.compareTo(id2) < 0
                ? "dm_" + id1 + "_" + id2
                : "dm_" + id2 + "_" + id1;
    }
}
