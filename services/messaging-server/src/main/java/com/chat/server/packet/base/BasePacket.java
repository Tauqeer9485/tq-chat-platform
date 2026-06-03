package com.chat.server.packet.base;

import com.chat.server.packet.type.PacketType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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