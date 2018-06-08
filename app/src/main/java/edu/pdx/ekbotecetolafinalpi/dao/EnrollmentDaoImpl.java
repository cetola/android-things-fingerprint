package edu.pdx.ekbotecetolafinalpi.dao;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.pdx.ekbotecetolafinalpi.account.Enrollment;
import edu.pdx.ekbotecetolafinalpi.account.User;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManager;

public class EnrollmentDaoImpl implements EnrollmentDao {
    private static final String TAG = "EnrollmentDaoImpl";
    private FirestoreManager dbManager;
    private FirebaseFirestore db;

    public EnrollmentDaoImpl(FirestoreManager dbManager) {
        this.dbManager = dbManager;
        db = dbManager.getDatabase();
    }

    @Override
    public void getEnrollmentByUser(User user, OnSuccessListener<DocumentReference> result) {

    }

    @Override
    public void saveEnrollment(Enrollment enrollment, OnSuccessListener<DocumentReference> result) {
        db.collection(Enrollment.COLLECTION)
                .add(enrollment)
                .addOnSuccessListener(result)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
}
