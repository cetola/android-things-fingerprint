package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.pdx.ekbotecetolafinalpi.exceptions.ConnectionFailedException;

public class FirestoreManagerImpl implements FirestoreManager {

    private static final String TAG = FirestoreManagerImpl.class.getSimpleName();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Date date;
    private FirebaseFirestore db;

    public FirestoreManagerImpl() {
        try {
            init();
        } catch (ConnectionFailedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int init() throws ConnectionFailedException {
        date = new Date();
        Log.i(TAG,"Database initialization starting: " + dateFormat.format(date));
        try {
            db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setTimestampsInSnapshotsEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
        } catch (Exception e) {
            throw new ConnectionFailedException();
        }
        return 0;
    }

    @Override
    public FirebaseFirestore getDatabase() {
        return db;
    }
}
