package com.chat.server.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PacketSerializer {

    private static final ObjectMapper mapper = new ObjectMapper();


    public static String toJson(Object packet) {
        try {
            return mapper.writeValueAsString(packet);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}