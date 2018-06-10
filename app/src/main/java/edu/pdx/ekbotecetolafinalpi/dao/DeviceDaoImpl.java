package edu.pdx.ekbotecetolafinalpi.dao;

import android.util.Log;

import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManager;
import edu.pdx.ekbotecetolafinalpi.realtime.RegisterFingerprintMsg;

public class DeviceDaoImpl implements DeviceDao {
    private static final String TAG = "DeviceDaoImpl";
    private FirestoreManager dbManager;

    public DeviceDaoImpl(FirestoreManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void sendMessage(String message) {
        Log.d(TAG, "------------------sendMessage: " + message);
        dbManager.setRealtimeData(RegisterFingerprintMsg.class.getSimpleName(), message);
    }
}
