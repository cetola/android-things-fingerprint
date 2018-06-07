package edu.pdx.ekbotecetolafinalpi.dao;

import android.util.Log;

public class DeviceDaoImpl implements DeviceDao {
    private static final String TAG = "DeviceDaoImpl";
    @Override
    public void sendMessage(String message) {
        Log.d(TAG, "------------------sendMessage: " + message);
    }
}
