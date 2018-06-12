package edu.pdx.ekbotecetolafinalpi;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.pdx.ekbotecetolafinalpi.account.CurrentUser;
import edu.pdx.ekbotecetolafinalpi.account.DeviceInfo;
import edu.pdx.ekbotecetolafinalpi.account.Enrollment;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDao;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDaoImpl;
import edu.pdx.ekbotecetolafinalpi.dao.EnrollmentDao;
import edu.pdx.ekbotecetolafinalpi.dao.EnrollmentDaoImpl;
import edu.pdx.ekbotecetolafinalpi.managers.EnrollmentManager;
import edu.pdx.ekbotecetolafinalpi.managers.EnrollmentManagerImpl;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManager;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManagerImpl;
import edu.pdx.ekbotecetolafinalpi.managers.GpioManager;
import edu.pdx.ekbotecetolafinalpi.managers.GpioManagerImpl;
import edu.pdx.ekbotecetolafinalpi.managers.IdentificationManager;
import edu.pdx.ekbotecetolafinalpi.managers.IdentificationManagerImpl;
import edu.pdx.ekbotecetolafinalpi.managers.UartManager;
import edu.pdx.ekbotecetolafinalpi.managers.UartManagerImpl;
import edu.pdx.ekbotecetolafinalpi.realtime.RegisterFingerprint;
import edu.pdx.ekbotecetolafinalpi.realtime.UnlockStatus;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    UartManager uartManager;
    GpioManager gpioManager;
    EnrollmentManager enrollmentManager;
    EnrollmentDao enrollmentDao;
    IdentificationManager identManager;
    DeviceInfo info;
    FirestoreManager dbManager;
    DeviceDao deviceDao;
    List<Integer> scannerIds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = new FirestoreManagerImpl();
        gpioManager = new GpioManagerImpl();
        enrollmentDao = new EnrollmentDaoImpl(dbManager);
        deviceDao = new DeviceDaoImpl(dbManager);
        uartManager = new UartManagerImpl(dbManager);
        scannerIds = new ArrayList<>();
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

    /**
     * We need to know which scanner IDs have been used already. By querying the Enrollment documents
     * we can make a list of the scanner IDs. For larger systems this might want to be its own
     * document collection.
     * @param user The current user attempting to enroll. This is required to pass along to the
     *             doEnroll function.
     */
    private void getScannerIdList(final CurrentUser user) {
        enrollmentDao.getEnrollments(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    Log.d(TAG, "No enrollments found.");
                } else {
                    List<Enrollment> enrollments = queryDocumentSnapshots.toObjects(Enrollment.class);
                    for(Enrollment e : enrollments) {
                        scannerIds.add(e.getScannerId());
                    }
                }
                doEnroll(user.getFingerId(), user.getUserId());
            }
        });
    }

    /**
     * Bind to the UnlockStatus and the RegisterFingerpring realtime fields. This tells us when
     * the Android Device is attempting to unlock or register a fingerprint (enroll).
     */
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

    /**
     * Check the value of the RegisterFingerprint realtime variable and wait for the START condition.
     * @param val The variable's new value.
     */
    private void enrollChange(String val) {
        if (val.equals(RegisterFingerprint.START)) {
            dbManager.getRealtimeData(CurrentUser.COLLECTION, new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    CurrentUser user = dataSnapshot.getValue(CurrentUser.class);
                    if(user != null) {
                        getScannerIdList(user);
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

    /**
     * Check the value of the UnlockStatus realtime variable and wait for the REQUEST condition.
     * @param val The variable's new value.
     */
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

    /**
     * The device info would be used if the app changes in the future to support things like
     * facial recognition scanners or retina scanners.
     * @param info
     */
    private void setDeviceInfo(DeviceInfo info) {
        this.info = info;
    }

    /**
     * Start the enrollment process
     * @param finger which finger the user is registering
     * @param userId the user id for the current user
     */
    private void doEnroll(int finger, String userId) {
        int scannerId = getScannerId();
        enrollmentManager = new EnrollmentManagerImpl(uartManager, dbManager);
        enrollmentManager.checkEnroll(scannerId, finger, userId);
    }

    /**
     * Return a value of 0-199 for a fingerprint ID.
     * Used fingerprint IDs are stored to the database.
     * @return
     */
    private int getScannerId() {
        int nextScannerId = 0;
        if(scannerIds.size() == 0) {
            return nextScannerId;
        } else {
            Collections.sort(scannerIds);
            return scannerIds.get(scannerIds.size()-1) + 1;
        }
    }

    /**
     * Start the identification process to unlock the box.
     * @param userId The user id trying to unlock.
     */
    private void doIdent(String userId) {
        identManager = new IdentificationManagerImpl(uartManager, dbManager, gpioManager);
        identManager.identifyFinger(userId);
    }

    /**
     * This method is for debugging and testing. Deletes all the data stored on the scanner.
     */
    private void doDeleteAll() {
        //DANGER ZONE
        enrollmentManager = new EnrollmentManagerImpl(uartManager, dbManager);
        enrollmentManager.deleteAll();
    }

    /**
     * Open the UART plugged into USB1.
     */
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