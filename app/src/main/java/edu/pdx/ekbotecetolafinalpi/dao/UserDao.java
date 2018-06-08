package edu.pdx.ekbotecetolafinalpi.dao;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import edu.pdx.ekbotecetolafinalpi.account.User;

public interface UserDao {
    DocumentReference getUserRef(String userId);
    void getUserById(String userId, OnSuccessListener<DocumentSnapshot> result);
    void saveUser(final User user, OnSuccessListener<DocumentReference> result);
}
