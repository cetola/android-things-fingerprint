package edu.pdx.ekbotecetolafinalpi.managers;

import android.os.Handler;
import android.os.HandlerThread;

public class ThreadedManager {
    private HandlerThread inputThread;
    private Handler inputHandler;

    protected void createLooperThread() {
        // Create a background looper thread for I/O
        inputThread = new HandlerThread("InputThread");
        inputThread.start();
        inputHandler = new Handler(inputThread.getLooper());
    }

    public Handler getInputHandler() {
        return inputHandler;
    }
}
