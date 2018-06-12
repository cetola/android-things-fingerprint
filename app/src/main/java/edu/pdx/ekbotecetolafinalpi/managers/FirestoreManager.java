package edu.pdx.ekbotecetolafinalpi.managers;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.pdx.ekbotecetolafinalpi.exceptions.ConnectionFailedException;
import edu.pdx.ekbotecetolafinalpi.uart.Message;

/**
 * Manages access to both the realtime database and the Firestore database.
 */
public interface FirestoreManager {

    /**
     * Gets the current database.
     * @return FirebaseFirestore database
     */
    FirebaseFirestore getDatabase();

    /**
     * Sets a realtime database value.
     * @param key Realtime database key
     * @param value value to set
     */
    void setRealtimeData(String key, Object value);

    /**
     * Binds a {@link ValueEventListener} to a realtime database value.
     * @param key Realtime database key
     * @param listener listener to bind
     */
    void bindRealtimeData(final String key, ValueEventListener listener);

    /**
     * Calls a {@link ValueEventListener} to get a realtime database value.
     * This only returns the value once, so the listener will only be called 1 time.
     * @param key Realtime database key
     * @param callback to be called only once
     */
    void getRealtimeData(String key, ValueEventListener callback);
}
