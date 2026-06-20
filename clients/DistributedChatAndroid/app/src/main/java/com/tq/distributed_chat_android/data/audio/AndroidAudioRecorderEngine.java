package com.tq.distributed_chat_android.data.audio;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import java.io.File;

public class AndroidAudioRecorderEngine implements AudioRecorderEngine {
    private static final String TAG = "AudioRecorderEngine";
    private final Context context;
    private MediaRecorder mediaRecorder;
    private boolean isRecordingActive = false;

    public AndroidAudioRecorderEngine(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void startRecording(File destinationFile) {
        if (isRecordingActive) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = new MediaRecorder(context);
            } else {
                mediaRecorder = new MediaRecorder();
            }

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);

            mediaRecorder.setOutputFile(destinationFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecordingActive = true;
            Log.d(TAG, "Hardware recording pipeline successfully initialized");
        } catch (Exception e) {
            Log.e(TAG, "Critical failure mapping audio stream parameters to device hardware", e);
            releaseResources();
        }
    }

    @Override
    public void stopRecording() {
        if (!isRecordingActive) return;
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Hardware stop invoked before tracking buffers received data", e);
        } finally {
            releaseResources();
        }
    }

    @Override
    public void cancelRecording() {
        releaseResources();
    }

    @Override
    public boolean isRecording() {
        return isRecordingActive;
    }

    private void releaseResources() {
        isRecordingActive = false;
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Resource clean teardown rejected by system device context", e);
            }
            mediaRecorder = null;
        }
    }
}