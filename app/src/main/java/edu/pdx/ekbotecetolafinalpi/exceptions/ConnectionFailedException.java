package edu.pdx.ekbotecetolafinalpi.exceptions;

import android.util.Log;

public class ConnectionFailedException extends Exception {
    private static final String TAG = "ConnectionFailedExcepti";
    public ConnectionFailedException() {
        super("The connection to the Firebase Cloud Firestore failed.");
        Log.e(TAG, "Failed to setup FirebaseDatabase.");
    }
}
