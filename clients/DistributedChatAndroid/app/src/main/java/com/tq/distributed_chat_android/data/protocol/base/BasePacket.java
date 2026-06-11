package com.tq.distributed_chat_android.data.protocol.base;

import com.tq.distributed_chat_android.data.protocol.type.PacketType;

public abstract class BasePacket {
    private PacketType packetType;

    protected BasePacket(PacketType packetType) {
        this.packetType = packetType;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }
}