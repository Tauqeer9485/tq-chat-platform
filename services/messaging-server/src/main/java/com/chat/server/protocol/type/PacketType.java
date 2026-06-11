package com.chat.server.protocol.type;

/**
 * Enum representing the type of packets exchanged between client and server
 * CHAT_MESSAGE: A chat message packet containing message details
 * MESSAGE_ACK: Acknowledgment for a received message
 * READ_RECEIPT: Notification that a message has been read
 * DELIVERED_RECEIPT: Notification that a message has been delivered
 * TYPING_INDICATOR: Indicates that a user is typing
 * USER_ONLINE: Notification that a user has come online
 * USER_OFFLINE: Notification that a user has gone offline
 * PING: Heartbeat packet sent by client to check server connectivity
 * PONG: Response to a PING packet from the server
 */
public enum PacketType {
    CHAT_MESSAGE,
    MESSAGE_ACK,
    READ_RECEIPT,
    DELIVERED_RECEIPT,
    TYPING_INDICATOR,
    USER_ONLINE,
    USER_OFFLINE,
    PING,
    PONG
}