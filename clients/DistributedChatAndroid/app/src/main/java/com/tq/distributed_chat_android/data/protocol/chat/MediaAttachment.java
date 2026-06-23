package com.tq.distributed_chat_android.data.protocol.chat;

public class MediaAttachment {
    private String mediaId;
    private String downloadUrl;
    private String fileName;
    private long fileSize;
    private String mimeType;
    private Integer durationSeconds;
    private Integer width;
    private Integer height;
    private String thumbnailUrl;

    public String getMediaId() {
        return mediaId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}