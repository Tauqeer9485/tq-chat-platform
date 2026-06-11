package com.chat.server.protocol.type;

/**
 * Enum representing the type of content in a message
 * TEXT: Plain text message
 * IMAGE: Image file
 * VIDEO: Video file
 * AUDIO: Audio file
 * FILE: Generic file attachment
 * SYSTEM: System-generated message (e.g., user joined, left)
 */
public enum ContentType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    SYSTEM
}   