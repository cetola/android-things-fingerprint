package edu.pdx.ekbotecetolafinalpi;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import edu.pdx.ekbotecetolafinalpi.account.CurrentUser;
import edu.pdx.ekbotecetolafinalpi.account.DeviceInfo;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDao;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDaoImpl;
import edu.pdx.ekbotecetolafinalpi.managers.EnrollmentManager;
import edu.pdx.ekbotecetolafinalpi.managers.EnrollmentManagerImpl;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManager;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManagerImpl;
import edu.pdx.ekbotecetolafinalpi.managers.IdentificationManager;
import edu.pdx.ekbotecetolafinalpi.managers.IdentificationManagerImpl;
import edu.pdx.ekbotecetolafinalpi.managers.UartManager;
import edu.pdx.ekbotecetolafinalpi.managers.UartManagerImpl;
import edu.pdx.ekbotecetolafinalpi.realtime.RegisterFingerprint;
import edu.pdx.ekbotecetolafinalpi.realtime.UnlockStatus;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    UartManager uartManager;
    EnrollmentManager enrollmentManager;
    IdentificationManager identManager;
    DeviceInfo info;
    FirestoreManager dbManager;
    DeviceDao deviceDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = new FirestoreManagerImpl();
        deviceDao = new DeviceDaoImpl(dbManager);
        uartManager = new UartManagerImpl(dbManager);
        uartManager.setDeviceInfoReadyListener(new UartManager.DeviceInfoReadyListener() {
            @Override
            public void onDeviceInfoReady(DeviceInfo info) {
                setDeviceInfo(info);
                bindValues();
            }
        });
        openUart();
        uartManager.getDeviceInfo();
    }

    private void bindValues() {
        dbManager.bindRealtimeData(RegisterFingerprint.COLLECTION, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String val = dataSnapshot.getValue(String.class);
                enrollChange(val);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "Bind canceled: " + RegisterFingerprint.COLLECTION);
            }
        });
        dbManager.bindRealtimeData(UnlockStatus.COLLECTION, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String val = dataSnapshot.getValue(String.class);
                identChange(val);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "Bind canceled: " + UnlockStatus.COLLECTION);
            }
        });
    }

    private void enrollChange(String val) {
        if (val.equals(RegisterFingerprint.START)) {
            dbManager.getRealtimeData(CurrentUser.COLLECTION, new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    CurrentUser user = dataSnapshot.getValue(CurrentUser.class);
                    if(user != null) {
                        doEnroll(user.getFingerId(), user.getUserId());
                    } else {
                        Log.e(TAG, "Could not find current user.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i(TAG, "Get real-time value failed: " + CurrentUser.COLLECTION);
                }
            });
        } else {
            //do nothing
            Log.d(TAG, "enrollChange: " + val);
        }
    }

    private void identChange(String val) {
        if(val.equals(UnlockStatus.REQUEST)) {
            dbManager.getRealtimeData(CurrentUser.COLLECTION, new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    CurrentUser user = dataSnapshot.getValue(CurrentUser.class);
                    if(user != null) {
                        doIdent(user.getUserId());
                    } else {
                        Log.e(TAG, "Could not find current user.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i(TAG, "Get real-time value failed: " + CurrentUser.COLLECTION);
                }
            });
        } else {
            //do nothing
            Log.d(TAG, "identChange: " + val);
        }
    }

    private void setDeviceInfo(DeviceInfo info) {
        this.info = info;
    }

    private void doEnroll(int finger, String userId) {
        int scannerId = getScannerId();
        enrollmentManager = new EnrollmentManagerImpl(uartManager, dbManager);
        enrollmentManager.checkEnroll(scannerId, finger, userId);
    }

    private int getScannerId() {
        return 0;
    }

    private void doIdent(String userId) {
        identManager = new IdentificationManagerImpl(uartManager, dbManager);
        identManager.identifyFinger(userId);
    }

    private void doDeleteAll() {
        //DANGER ZONE
        enrollmentManager.deleteAll();
    }

    private void openUart() {
        List<String> devices = uartManager.getDeviceList();
        String myDevice = "";
        for(String device : devices) {
            Log.d(TAG, "openUart: Found USB UART: " + device);
            //TODO: meh.
            if(device.contains("USB1")) {
                myDevice = device;
            }
        }
        uartManager.openUsbUart(myDevice);
    }
}