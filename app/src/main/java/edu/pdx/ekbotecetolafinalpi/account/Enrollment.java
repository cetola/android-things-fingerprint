package edu.pdx.ekbotecetolafinalpi.account;

import com.google.firebase.firestore.DocumentReference;

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

    public User getUser() {
        return user;
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

    public DocumentReference getUserRef() {
        return userRef;
    }
}
