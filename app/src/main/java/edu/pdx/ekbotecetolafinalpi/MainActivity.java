package edu.pdx.ekbotecetolafinalpi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import java.util.List;

import edu.pdx.ekbotecetolafinalpi.account.DeviceInfo;
import edu.pdx.ekbotecetolafinalpi.managers.EnrollmentManager;
import edu.pdx.ekbotecetolafinalpi.managers.EnrollmentManagerImpl;
import edu.pdx.ekbotecetolafinalpi.managers.UartManager;
import edu.pdx.ekbotecetolafinalpi.managers.UartManagerImpl;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    UartManager uartManager;
    EnrollmentManager enrollmentManager;
    DeviceInfo info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uartManager = new UartManagerImpl();
        uartManager.setDeviceInfoReadyListener(new UartManager.DeviceInfoReadyListener() {
            @Override
            public void onDeviceInfoReady(DeviceInfo info) {
                setDeviceInfo(info);
            }
        });
        openUart();
        enrollmentManager = new EnrollmentManagerImpl(uartManager);
        uartManager.getDeviceInfo();
    }

    private void setDeviceInfo(DeviceInfo info) {
        this.info = info;
        enrollmentManager.startEnrollment();
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