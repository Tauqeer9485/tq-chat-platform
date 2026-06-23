package com.tq.distributed_chat_android.data.remote;

import android.net.Uri;
import com.tq.distributed_chat_android.data.model.MediaMetadata;

public class MediaUploadTask {
    private final Uri fileUri;
    private final MediaMetadata preCalculatedMetadata;

    public MediaUploadTask(Uri fileUri) {
        this(fileUri, new MediaMetadata());
    }

    public MediaUploadTask(Uri fileUri, MediaMetadata preCalculatedMetadata) {
        this.fileUri = fileUri;
        this.preCalculatedMetadata = preCalculatedMetadata != null ? preCalculatedMetadata : new MediaMetadata();
    }

    public Uri getFileUri() { return fileUri; }
    public MediaMetadata getPreCalculatedMetadata() { return preCalculatedMetadata; }
}