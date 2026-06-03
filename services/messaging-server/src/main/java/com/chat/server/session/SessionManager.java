package com.chat.server.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages user sessions with UUID-based identification.
 * Thread-safe using ConcurrentHashMap.
 */
public class SessionManager {
    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());

    private static final Map<UserId, Session> sessions = new ConcurrentHashMap<>();

    public static void addSession(Session session) {
        if (session == null) {
            logger.warning("Attempted to add null session");
            return;
        }
        sessions.put(session.getUserId(), session);
        logger.info("Session added for user: " + session.getUsername() + " (" + session.getUserId() + ")");
    }

    public static void removeSession(UserId userId) {
        if (userId == null) {
            logger.warning("Attempted to remove session with null userId");
            return;
        }
        Session removed = sessions.remove(userId);
        if (removed != null) {
            logger.info("Session removed for user: " + removed.getUsername());
        }
    }

    public static Session getSession(UserId userId) {
        if (userId == null) {
            return null;
        }
        return sessions.get(userId);
    }

    public static boolean isOnline(UserId userId) {
        if (userId == null) {
            return false;
        }
        return sessions.containsKey(userId);
    }

    public static void sendToUser(UserId userId, Object packet) {
        if (userId == null || packet == null) {
            logger.warning("sendToUser called with null userId or packet");
            return;
        }
        Session session = sessions.get(userId);
        if (session == null) {
            logger.fine("User offline: " + userId);
            return;
        }
        try {
            session.sendPacket(packet);
        } catch (Exception e) {
            logger.warning("Failed to send packet to user " + userId + ": " + e.getMessage());
        }
    }
}