package edu.pdx.ekbotecetolafinalpi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.pdx.ekbotecetolafinalpi.Uart.Command;
import edu.pdx.ekbotecetolafinalpi.Uart.CommandList;
import edu.pdx.ekbotecetolafinalpi.Uart.Response;
import edu.pdx.ekbotecetolafinalpi.Uart.UartUtils;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private PeripheralManager pm;
    // UART Configuration Parameters
    private static final int BAUD_RATE = 9600;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;

    private static final int CHUNK_SIZE = 512;

    private HandlerThread mInputThread;
    private Handler mInputHandler;

    private UartDevice uartDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "UART Start");

        // Create a background looper thread for I/O
        mInputThread = new HandlerThread("InputThread");
        mInputThread.start();
        mInputHandler = new Handler(mInputThread.getLooper());

        pm = PeripheralManager.getInstance();
        List<String> deviceList = pm.getUartDeviceList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No UART port available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
        }

        // Attempt to access the UART device
        try {
            openUart("USB1-1.4:1.0", BAUD_RATE);
            // Read any initially buffered data
            mInputHandler.post(mTransferUartRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open UART device", e);

        }
        Command c = new Command();
        c.setParams(0);
        c.setCmd(CommandList.Open);
        Log.d(TAG, "onCreate: Write to UART: " + c.toString());
        c.sendCommand(uartDevice);
    }

    private Runnable mTransferUartRunnable = new Runnable() {
        @Override
        public void run() {
            readData();
        }
    };

    /**
     * Access and configure the requested UART device for 8N1.
     *
     * @param name Name of the UART peripheral device to open.
     * @param baudRate Data transfer rate. Should be a standard UART baud,
     *                 such as 9600, 19200, 38400, 57600, 115200, etc.
     *
     * @throws IOException if an error occurs opening the UART port.
     */
    private void openUart(String name, int baudRate) throws IOException {
        Log.d(TAG, "openUart: opening");
        uartDevice = PeripheralManager.getInstance().openUartDevice(name);
        // Configure the UART
        uartDevice.setBaudrate(baudRate);
        uartDevice.setDataSize(DATA_BITS);
        uartDevice.setParity(UartDevice.PARITY_NONE);
        uartDevice.setStopBits(STOP_BITS);
        uartDevice.setHardwareFlowControl(UartDevice.HW_FLOW_CONTROL_NONE);

        uartDevice.registerUartDeviceCallback(mInputHandler, mCallback);
    }

    /**
     * Callback invoked when UART receives new incoming data.
     */
    private UartDeviceCallback mCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            readData();
            //Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };


    private void readData() {
        if (uartDevice != null) {
            Response resp = new Response();
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                while ((read = uartDevice.read(buffer, buffer.length)) > 0) {
                    Log.i(TAG, "readData: " + UartUtils.bytesToHex(buffer, read) + " size " + read);
                    resp.addBytes(Arrays.copyOfRange(buffer, 0, read));
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }
            if(!resp.isEmpty()) {
                Log.i(TAG, "readData: Got reponse: " + resp.toString());
                if(!resp.getAck()) {
                    Log.e(TAG, "readData: NACK!");
                    Log.d(TAG, "readData: ERROR TEXT: " + resp.getError());
                } else {
                    Log.d(TAG, "readData: ACK!");
                }
            }
        }
    }
}