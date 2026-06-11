package com.chat.server.protocol.base;

import com.chat.server.protocol.type.PacketType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * BasePacket is the parent class for all packet types sent between client and server.
 * It contains common fields and serves as a base for specific packet implementations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasePacket {

    private PacketType packetType;

    public BasePacket() {
    }

    public BasePacket(PacketType packetType) {
        this.packetType = packetType;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }
}