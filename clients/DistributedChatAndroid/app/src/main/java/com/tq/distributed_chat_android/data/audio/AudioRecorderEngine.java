package com.tq.distributed_chat_android.data.audio;

import java.io.File;

public interface AudioRecorderEngine {
    void startRecording(File destinationFile);
    void stopRecording();
    void cancelRecording();
    boolean isRecording();
}