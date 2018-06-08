package edu.pdx.ekbotecetolafinalpi.dao;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import edu.pdx.ekbotecetolafinalpi.account.Enrollment;
import edu.pdx.ekbotecetolafinalpi.account.User;

public interface EnrollmentDao {
    void getEnrollmentByUser(User user, OnSuccessListener<DocumentReference> result);
    void saveEnrollment(Enrollment enrollment, OnSuccessListener<DocumentReference> result);
}
