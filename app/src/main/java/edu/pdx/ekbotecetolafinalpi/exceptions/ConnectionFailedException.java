package edu.pdx.ekbotecetolafinalpi.exceptions;

import android.util.Log;

public class ConnectionFailedException extends Exception {
    public ConnectionFailedException() {
        super("The connection to the Firebase Cloud Firestore failed.");
        Log.e("ERR", "Failed to setup FirebaseDatabase.");
    }
}
