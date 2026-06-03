package com.chat.server.service;

import com.chat.server.packet.chat.MessagePacket;
import com.chat.server.packet.type.MessageStatus;
import com.chat.server.session.Session;
import com.chat.server.session.SessionManager;
import com.chat.server.session.UserId;

import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for processing and delivering messages.
 */
public class MessageService {
    private static final Logger logger = Logger.getLogger(MessageService.class.getName());
    
    private final ConversationService conversationService = ConversationService.getInstance();
    
    /**
     * Process an incoming message packet and deliver to all members in conversation.
     * Validates sender, serializes message, and sends to all recipients.
     * 
     * @param packet the message packet to process
     */
    public void processMessage(MessagePacket packet) {
        if (packet == null) {
            logger.warning("Null packet received in processMessage");
            return;
        }
        if (packet.getConversationId() == null || packet.getConversationId().isEmpty()) {
            logger.warning("Packet has invalid conversationId");
            return;
        }

        packet.setServerTimestamp(System.currentTimeMillis());

        packet.setStatus(MessageStatus.DELIVERED);

        boolean validMember = conversationService.isMember(packet.getConversationId(), packet.getSenderId());

        if (!validMember) {
            logger.warning("User " + packet.getSenderId() + " not in conversation " + packet.getConversationId());
            return;
        }

        Set<UserId> members = conversationService.getMembers(packet.getConversationId());

        for (UserId memberId : members) {
            if (memberId.equals(packet.getSenderId())) {
                continue;
            }

            Session session = SessionManager.getSession(memberId);

            if (session == null) {
                logger.fine("User offline: " + memberId);
                continue;
            }

            try {
                session.sendPacket(packet);
                logger.fine("Message delivered to user: " + memberId);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to send message to user " + memberId, e);
            }
        }
    }
}