package com.chat.server.protocol.type;

/**
 * Enum representing the status of a message
 * SENT: Message has been sent by the sender
 * DELIVERED: Message has been delivered to the recipient's device
 * READ: Recipient has read the message
 * FAILED: Message failed to send
 */
public enum MessageStatus {
    SENT,
    DELIVERED,
    READ,
    FAILED
}