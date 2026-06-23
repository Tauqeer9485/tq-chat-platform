package com.tq.distributed_chat_android.data.protocol.chat;

import com.tq.distributed_chat_android.data.protocol.type.AttachmentType;
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