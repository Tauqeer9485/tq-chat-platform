package com.tq.distributed_chat_android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MediaUploadResponse {
    @SerializedName("id")
    private final String mediaId;

    @SerializedName("conversation_id")
    private final String conversationId;

    @SerializedName("uploader_id")
    private final String uploaderId;

    @SerializedName("file_name")
    private final String fileName;

    @SerializedName("mime_type")
    private final String mimeType;

    @SerializedName("size")
    private final String size;

    @SerializedName("extension")
    private final String extension;

    @SerializedName("storage_key")
    private final String storageKey;

    @SerializedName("status")
    private final String status;

    @SerializedName("createdAt")
    private final String createdAt;

    @SerializedName("updatedAt")
    private final String updatedAt;

    @SerializedName("downloadUrl")
    private final String downloadUrl;

    public MediaUploadResponse(String mediaId, String conversationId, String uploaderId,
                               String fileName, String mimeType, String size,
                               String extension, String storageKey, String status,
                               String createdAt, String updatedAt, String downloadUrl) {
        this.mediaId = mediaId;
        this.conversationId = conversationId;
        this.uploaderId = uploaderId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.size = size;
        this.extension = extension;
        this.storageKey = storageKey;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.downloadUrl = downloadUrl;
    }

    public String getMediaId() { return mediaId; }
    public String getConversationId() { return conversationId; }
    public String getUploaderId() { return uploaderId; }
    public String getFileName() { return fileName; }
    public String getMimeType() { return mimeType; }
    public String getSize() { return size; }
    public String getExtension() { return extension; }
    public String getStorageKey() { return storageKey; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getDownloadUrl() { return downloadUrl; }
}