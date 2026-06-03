package com.chat.server.dispatcher;

import com.chat.server.handler.PacketHandler;
import com.chat.server.packet.base.BasePacket;
import com.chat.server.packet.type.PacketType;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Map;

public class PacketDispatcher {
    private static final Logger logger = Logger.getLogger(PacketDispatcher.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper();
    private final Map<PacketType, PacketHandlerRegistration<?>> handlers = new HashMap<>();

    public <T extends BasePacket>
    
    void registerHandler(PacketType packetType, Class<T> packetClass, PacketHandler<T> handler) {
        handlers.put(packetType, new PacketHandlerRegistration<>(packetClass, handler));
    }

    @SuppressWarnings("unchecked")
    public void dispatch(ChannelHandlerContext ctx, String rawJson) throws Exception {
        BasePacket basePacket = mapper.readValue(rawJson, BasePacket.class);
        PacketType packetType = basePacket.getPacketType();
        PacketHandlerRegistration<?> registration = handlers.get(packetType);

        if (registration == null) {
            logger.warning("No handler for packet: " + packetType);
            return;
        }

        Object packet = mapper.readValue(rawJson, registration.packetClass);
        PacketHandler<Object> handler = (PacketHandler<Object>) registration.handler;

        handler.handle(ctx, packet);
    }

    private static class PacketHandlerRegistration<T> {
        private final Class<T> packetClass;
        private final PacketHandler<T> handler;

        public PacketHandlerRegistration(Class<T> packetClass, PacketHandler<T> handler) {
            this.packetClass = packetClass;
            this.handler = handler;
        }
    }
}
