package edu.pdx.ekbotecetolafinalpi.dao;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public interface UserDao {
    DocumentReference getUserRef(String userId);
    void getUserById(String userId, OnSuccessListener<DocumentSnapshot> result);
}
