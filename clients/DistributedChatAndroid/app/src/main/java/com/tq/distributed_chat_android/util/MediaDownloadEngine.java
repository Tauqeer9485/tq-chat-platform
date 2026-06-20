package com.tq.distributed_chat_android.util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MediaDownloadEngine {
    private final Context context;
    private final OkHttpClient httpClient;

    public interface DownloadCallback {
        void onSuccess(Uri localFileUri);
        void onFailure(Exception e);
    }

    public MediaDownloadEngine(Context context) {
        this.context = context.getApplicationContext();
        this.httpClient = new OkHttpClient();
    }

    public void downloadFileAsync(String urlString, String fileName, DownloadCallback callback) {
        String fixedUrl = urlString;
        if (fixedUrl.contains("localhost")) {
            fixedUrl = fixedUrl.replace("localhost", "192.168.43.248");
        }

        Request request = new Request.Builder().url(fixedUrl).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(new IOException("Server error")));
                    return;
                }

                try {
                    File destinationFile = new File(context.getExternalCacheDir(), fileName);
                    try (InputStream inputStream = response.body().byteStream();
                         FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }

                    Uri localUri = Uri.fromFile(destinationFile);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(localUri));

                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
                }
            }
        });
    }
}