package edu.pdx.ekbotecetolafinalpi.managers;

import com.google.firebase.firestore.FirebaseFirestore;

import edu.pdx.ekbotecetolafinalpi.exceptions.ConnectionFailedException;
import edu.pdx.ekbotecetolafinalpi.uart.Message;

public interface FirestoreManager {
    int init() throws ConnectionFailedException;
    FirebaseFirestore getDatabase();
}
