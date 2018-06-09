package edu.pdx.ekbotecetolafinalpi.dao;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

import edu.pdx.ekbotecetolafinalpi.account.History;

public interface HistoryDao {
    void getHistory(OnSuccessListener<QuerySnapshot> result);
    void saveHistory(History history, OnSuccessListener<DocumentReference> result);
}
