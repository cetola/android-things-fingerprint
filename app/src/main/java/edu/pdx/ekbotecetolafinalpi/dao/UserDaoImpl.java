package edu.pdx.ekbotecetolafinalpi.dao;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import edu.pdx.ekbotecetolafinalpi.account.User;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManager;

public class UserDaoImpl implements UserDao {
    private static final String TAG = "UserDaoImpl";
    private FirestoreManager dbManager;
    private FirebaseFirestore db;

    public UserDaoImpl(FirestoreManager dbManager) {
        this.dbManager = dbManager;
        db = dbManager.getDatabase();
    }

    @Override
    public DocumentReference getUserRef(String userId) {
        Log.d(TAG, "getUserRef: " + userId);
        return dbManager.getDatabase().collection(User.COLLECTION).document(userId);
    }

    @Override
    public void getUserById(String userId, OnSuccessListener<DocumentSnapshot> result) {
        DocumentReference docRef = db.collection(User.COLLECTION).document(userId);
        docRef.get().addOnSuccessListener(result)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to query database for object(s): " + User.COLLECTION);
                    }
                });
    }

    @Override
    public void getUserByUsername(String username, OnSuccessListener<QuerySnapshot> result) {
        db.collection(User.COLLECTION).whereEqualTo("username", username)
                .get().addOnSuccessListener(result)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to query database for object(s): " + User.COLLECTION);
                    }
                });
    }

    @Override
    public void saveUser(final User user, OnSuccessListener<DocumentReference> result) {
        db.collection(User.COLLECTION)
                .add(user)
                .addOnSuccessListener(result)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding user", e);
                    }
                });
    }
}
