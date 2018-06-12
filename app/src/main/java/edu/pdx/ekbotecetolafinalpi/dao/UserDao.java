package edu.pdx.ekbotecetolafinalpi.dao;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import edu.pdx.ekbotecetolafinalpi.account.User;

/**
 * User Data Access Object
 */
public interface UserDao {

    /**
     * Since an enrollment has an associated User, it is handy to be able to get a given user
     * ref from the database given a string ID.
     * @param userId
     * @return
     */
    DocumentReference getUserRef(String userId);

    /**
     * Gets a user given a string ID. Takes a listener for when the result is received from the
     * database engine.
     * @param userId
     * @param result
     */
    void getUserById(String userId, OnSuccessListener<DocumentSnapshot> result);

    /**
     * Get a user by the username. Handy for the Android Device app.
     * @param username
     * @param result
     */
    void getUserByUsername(String username, OnSuccessListener<QuerySnapshot> result);

    /**
     * Save a user to the database. Handy for the Android Device app.
     * @param user
     * @param result
     */
    void saveUser(final User user, OnSuccessListener<DocumentReference> result);
}
