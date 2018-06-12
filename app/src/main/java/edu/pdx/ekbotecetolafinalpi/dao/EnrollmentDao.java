package edu.pdx.ekbotecetolafinalpi.dao;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

import edu.pdx.ekbotecetolafinalpi.account.Enrollment;
import edu.pdx.ekbotecetolafinalpi.account.User;

/**
 * Data Access Object for the Enrollments
 */
public interface EnrollmentDao {

    /**
     * Persist an enrollment to the document database.
     * @param enrollment
     * @param result
     */
    void saveEnrollment(Enrollment enrollment, OnSuccessListener<DocumentReference> result);

    /**
     * Get all the enrollments. This is handy to see which scanner ID's have been used.
     * @param results
     */
    void getEnrollments(OnSuccessListener<QuerySnapshot> results);
}
