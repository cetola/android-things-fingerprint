package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.pdx.ekbotecetolafinalpi.exceptions.ConnectionFailedException;

public class FirestoreManagerImpl implements FirestoreManager {

    private static final String TAG = FirestoreManagerImpl.class.getSimpleName();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Date date;
    private DatabaseReference mDatabase;

    @Override
    public int init() throws ConnectionFailedException {
        date = new Date();
        Log.i(TAG,"Database initialization starting: " + dateFormat.format(date));
        try {
            mDatabase = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            throw new ConnectionFailedException();
        }
        return 0;
    }
}
