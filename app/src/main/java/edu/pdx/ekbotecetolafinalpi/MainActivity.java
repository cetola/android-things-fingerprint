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
        Log.d(TAG, "UART Start");
        uartManager = new UartManagerImpl();
        List<String> devices = uartManager.getDeviceList();
        //TODO: get this dynamically
        uartManager.openUsbUart("USB1-1.4:1.0");
        sendTestCommand();
    }

    private void sendTestCommand() {
        Command c = new Command();
        c.setParams(1);
        c.setCmd(CommandList.Open);
        Log.d(TAG, "onCreate: Write to UART: " + c.toString());
        uartManager.sendCommand(c);
    }
}