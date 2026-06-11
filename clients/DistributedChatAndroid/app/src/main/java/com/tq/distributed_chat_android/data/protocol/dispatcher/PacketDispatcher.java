package com.tq.distributed_chat_android.data.protocol.dispatcher;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tq.distributed_chat_android.data.protocol.base.BasePacket;
import com.tq.distributed_chat_android.data.protocol.handler.PacketHandler;
import com.tq.distributed_chat_android.data.protocol.type.PacketType;

import java.util.HashMap;
import java.util.Map;

public class PacketDispatcher {

    private static final String TAG = "PacketDispatcher";
    private final Gson gson = new Gson();
    private final Map<PacketType, PacketHandlerRegistration<?>> handlers = new HashMap<>();

    public <T extends BasePacket> void registerHandler(
            PacketType packetType,
            Class<T> packetClass,
            PacketHandler<T> handler
    ) {
        handlers.put(
                packetType,
                new PacketHandlerRegistration<>(packetClass, handler)
        );
    }

    @SuppressWarnings("unchecked")
    public void dispatch(String rawJson) {
        try {
            JsonObject jsonObject = JsonParser.parseString(rawJson).getAsJsonObject();

            if (!jsonObject.has("packetType")) {
                Log.w(TAG, "Incoming payload missing structural 'packetType' key field.");
                return;
            }

            String packetTypeStr = jsonObject.get("packetType").getAsString();
            PacketType packetType = PacketType.valueOf(packetTypeStr);

            PacketHandlerRegistration<?> registration = handlers.get(packetType);
            if (registration == null) {
                Log.w(TAG, "No handler registered for packet type: " + packetType);
                return;
            }

            Object packet = gson.fromJson(rawJson, registration.packetClass);

            PacketHandler<Object> handler = (PacketHandler<Object>) registration.handler;
            handler.handle(packet);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unknown or unmapped PacketType enum value received", e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to dispatch packet (Ask Gemini)", e);
        }
    }

    private static class PacketHandlerRegistration<T> {
        private final Class<T> packetClass;
        private final PacketHandler<T> handler;

        public PacketHandlerRegistration(
                Class<T> packetClass,
                PacketHandler<T> handler
        ) {
            this.packetClass = packetClass;
            this.handler = handler;
        }
    }
}