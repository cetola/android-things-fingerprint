package edu.pdx.ekbotecetolafinalpi.dao;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

import edu.pdx.ekbotecetolafinalpi.account.History;

public interface HistoryDao {

    /**
     * Gets the entire history collection.
     * @param result
     */
    void getHistory(OnSuccessListener<QuerySnapshot> result);

    /**
     * Saves a single history event.
     * @param history
     * @param result
     */
    void saveHistory(History history, OnSuccessListener<DocumentReference> result);
}
