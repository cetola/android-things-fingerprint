package edu.pdx.ekbotecetolafinalpi.account;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

public class Enrollment {
    private String id;
    private int scannerId;
    private int finger;
    private User user;
    private DocumentReference userRef;
    public static final String COLLECTION = "enrollments";

    public Enrollment(int scannerId, int finger, User user, DocumentReference userRef) {
        setScannerId(scannerId);
        setFinger(finger);
        setUser(user, userRef);
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFinger() {
        return finger;
    }

    public void setFinger(int finger) {
        this.finger = finger;
    }

    public void setUser(User user, DocumentReference userRef) {
        this.user = user;
        this.userRef = userRef;
    }

    public int getScannerId() {
        return scannerId;
    }

    public void setScannerId(int scannerId) {
        this.scannerId = scannerId;
    }

    /**
     * This is simply for debugging purposes.
     *
     * This will store a String "user name" in the Firestore. While this is redundant data, it
     * makes debugging much easier as a quick glance at the Firestore tells you which user is
     * associated with this enrollment.
     * @return
     */
    @SuppressWarnings("unused")
    public String getUserName() {
        return this.user.getUsername();
    }

    @SuppressWarnings("unused")
    public DocumentReference getUserRef() {
        return userRef;
    }
}
