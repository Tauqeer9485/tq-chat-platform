package com.chat.server.protocol.chat;

import com.chat.server.protocol.type.AttachmentType;

public class MessageAttachment {

    private AttachmentType type;
    private MediaAttachment media;

    public AttachmentType getType() {
        return type;
    }

    public void setType(AttachmentType type) {
        this.type = type;
    }

    public MediaAttachment getMedia() {
        return media;
    }

    public void setMedia(MediaAttachment media) {
        this.media = media;
    }
}