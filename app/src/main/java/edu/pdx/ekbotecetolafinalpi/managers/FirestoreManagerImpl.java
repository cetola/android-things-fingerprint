package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private DatabaseReference rtDb;

    public FirestoreManagerImpl() {
        try {
            init();
        } catch (ConnectionFailedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup access to both databases. rtDb = realtime    db = Firestore
     * @return
     * @throws ConnectionFailedException
     */
    private int init() throws ConnectionFailedException {
        date = new Date();
        Log.i(TAG,"Database initialization starting: " + dateFormat.format(date));
        try {
            rtDb = FirebaseDatabase.getInstance().getReference();
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

    @Override
    public void setRealtimeData(String key, Object value) {
        rtDb.child(key).setValue(value);
    }

    @Override
    public void bindRealtimeData(final String key, ValueEventListener listener) {
        rtDb.child(key).addValueEventListener(listener);
    }

    @Override
    public void getRealtimeData(String key, ValueEventListener callback) {
        rtDb.child(key).addListenerForSingleValueEvent(callback);
    }
}
