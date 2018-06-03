package edu.pdx.ekbotecetolafinalpi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import java.util.List;
import edu.pdx.ekbotecetolafinalpi.managers.UartManager;
import edu.pdx.ekbotecetolafinalpi.managers.UartManagerImpl;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandList;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    UartManager uartManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uartManager = new UartManagerImpl();
        List<String> devices = uartManager.getDeviceList();
        String myDevice = "";
        for(String device : devices) {
            Log.d(TAG, "onCreate: Found USB UART: " + device);
            if(device.contains("USB1")) {
                myDevice = device;
            }
        }
        //TODO: get this dynamically
        uartManager.openUsbUart(myDevice);
        sendTestCommand();
    }

    private void sendTestCommand() {
        Command c = new Command();
        c.setParams(1);
        c.setCmd(CommandList.Open);
        uartManager.sendCommand(c);
    }
}