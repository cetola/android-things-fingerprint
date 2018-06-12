package edu.pdx.ekbotecetolafinalpi.managers;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * For any manager that needs to have an input handler and thread, this class acts as a parent
 * class for that manager. Since only 1 class currently uses this functionality, this is a very
 * simple class. It would need to be improved if it were to be used for multiple subclasses.
 */
public class ThreadedManager {
    private HandlerThread inputThread;
    private Handler inputHandler;

    /**
     * Create a looper and attach it to the handler.
     */
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
