package com.chat.server.packet.type;

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