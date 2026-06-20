package com.tq.distributed_chat_android.data.remote.dto;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import java.io.IOException;
import java.io.InputStream;

public class ContentUriRequest extends RequestBody {
    private final Context context;
    private final Uri uri;
    private final String mimeType;
    private final long fileSize;

    public ContentUriRequest(Context context, Uri uri) {
        this.context = context.getApplicationContext();
        this.uri = uri;

        String type = context.getContentResolver().getType(uri);

        if (type == null || "application/octet-stream".equalsIgnoreCase(type)) {
            String fileName = getFileName();
            if (fileName != null && fileName.contains(".")) {
                String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
                if (extension == null || extension.isEmpty()) {
                    extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                }

                String inferredType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                if (inferredType != null) {
                    type = inferredType;
                } else if ("m4a".equalsIgnoreCase(extension)) {
                    type = "audio/x-m4a";
                }
            }
        }

        this.mimeType = (type != null) ? type : "application/octet-stream";
        this.fileSize = determineFileSize();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(mimeType);
    }

    @Override
    public long contentLength() {
        return fileSize;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Failed to resolve absolute system input stream for source URI: " + uri);
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                sink.write(buffer, 0, bytesRead);
            }
        }
    }

    public String getFileName() {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) return cursor.getString(nameIndex);
            }
        }
        String path = uri.getPath();
        if (path == null) return "unknown_file";
        int cut = path.lastIndexOf('/');
        return (cut != -1) ? path.substring(cut + 1) : path;
    }

    private long determineFileSize() {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) return cursor.getLong(sizeIndex);
            }
        }
        return -1;
    }
}