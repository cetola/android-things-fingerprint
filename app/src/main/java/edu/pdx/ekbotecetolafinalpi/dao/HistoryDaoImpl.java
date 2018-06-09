package edu.pdx.ekbotecetolafinalpi.dao;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import edu.pdx.ekbotecetolafinalpi.account.History;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManager;

public class HistoryDaoImpl implements HistoryDao {
    private static final String TAG = "HistoryDaoImpl";
    private FirestoreManager dbManager;
    private FirebaseFirestore db;
    
    public HistoryDaoImpl(FirestoreManager dbManager) {
        this.dbManager = dbManager;
        db = dbManager.getDatabase();
    }
    @Override
    public void getHistory(OnSuccessListener<QuerySnapshot> result) {
        CollectionReference docRef = db.collection(History.COLLECTION);
        docRef.get().addOnSuccessListener(result)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to query database for object(s): " + History.COLLECTION);
                    }
                });
    }

    @Override
    public void saveHistory(History history, OnSuccessListener<DocumentReference> result) {
        db.collection(History.COLLECTION)
                .add(history)
                .addOnSuccessListener(result)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding user", e);
                    }
                });
    }
}
