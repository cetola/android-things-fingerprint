package edu.pdx.ekbotecetolafinalpi.managers;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.pdx.ekbotecetolafinalpi.exceptions.ConnectionFailedException;
import edu.pdx.ekbotecetolafinalpi.uart.Message;

public interface FirestoreManager {
    FirebaseFirestore getDatabase();
    void setRealtimeData(String key, Object value);
    void bindRealtimeData(final String key, ValueEventListener listener);
    void getRealtimeData(String key, ValueEventListener callback);
}
