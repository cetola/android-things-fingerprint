package edu.pdx.ekbotecetolafinalpi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.pdx.ekbotecetolafinalpi.account.DeviceInfo;
import edu.pdx.ekbotecetolafinalpi.managers.UartManager;
import edu.pdx.ekbotecetolafinalpi.managers.UartManagerImpl;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandList;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    UartManager uartManager;
    DeviceInfo info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uartManager = new UartManagerImpl();
        List<String> devices = uartManager.getDeviceList();
        String myDevice = "";
        for(String device : devices) {
            Log.d(TAG, "onCreate: Found USB UART: " + device);
            //TODO: meh.
            if(device.contains("USB1")) {
                myDevice = device;
            }
        }
        uartManager.openUsbUart(myDevice);
        uartManager.sendCommand(new Command(1, CommandList.Open));
        holdOnASec();
        info = uartManager.getDeviceInfo();
        printDeviceInfo();
        uartManager.sendCommand(new Command(0, CommandList.Open));
        holdOnASec();
        uartManager.sendCommand(new Command(0, CommandList.GetEnrollCount));
        holdOnASec();
        Log.d(TAG, "onCreate: Total Enrolled: " + uartManager.getResponse().getParams());
    }

    private void printDeviceInfo() {
        Log.i(TAG, "printDeviceInfo: " + info.toString());
    }

    private void holdOnASec() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}