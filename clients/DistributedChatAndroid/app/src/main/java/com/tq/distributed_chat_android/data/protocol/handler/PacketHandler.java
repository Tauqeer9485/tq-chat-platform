package com.tq.distributed_chat_android.data.protocol.handler;

public interface PacketHandler<T> {
    void handle(T packet) throws Exception;
}
