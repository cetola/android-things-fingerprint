package edu.pdx.ekbotecetolafinalpi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.pdx.ekbotecetolafinalpi.Uart.Command;
import edu.pdx.ekbotecetolafinalpi.Uart.CommandList;
import edu.pdx.ekbotecetolafinalpi.Uart.DataPacket;
import edu.pdx.ekbotecetolafinalpi.Uart.Message;
import edu.pdx.ekbotecetolafinalpi.Uart.Response;
import edu.pdx.ekbotecetolafinalpi.Uart.UartUtils;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private PeripheralManager pm;
    //TODO: create a uart device manager
    private static final int BAUD_RATE = 9600;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;

    private static final int CHUNK_SIZE = 512;
    private ByteArrayOutputStream data = new ByteArrayOutputStream();
    private int oldDataSize;

    private HandlerThread mInputThread;
    private Handler mInputHandler;

    //TODO: watch this device. We don't want people sending messages when we are receiving. (manager)
    private UartDevice uartDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "UART Start");
        pm = PeripheralManager.getInstance();
        listUartDevices();
        createLooperThread();
        //TODO: get this dynamically
        openUsbUart("USB1-1.4:1.0");
        sendTestCommand();
    }

    private void listUartDevices() {
        List<String> deviceList = pm.getUartDeviceList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No UART port available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
        }
    }

    private void openUsbUart(String uartName) {
        try {
            openUart(uartName, BAUD_RATE);
            mInputHandler.post(mTransferUartRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open UART device", e);

        }
    }

    private void createLooperThread() {
        // Create a background looper thread for I/O
        mInputThread = new HandlerThread("InputThread");
        mInputThread.start();
        mInputHandler = new Handler(mInputThread.getLooper());
    }

    private void sendTestCommand() {
        Command c = new Command();
        c.setParams(1);
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
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                while ((read = uartDevice.read(buffer, buffer.length)) > 0) {
                    //Log.i(TAG, "readData: " + UartUtils.bytesToHex(buffer, read) + " size " + read);
                    data.write(buffer, 0, read);
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }
            if(data.size() > 0) {
                waitForData();
            }
        }
    }

    //TODO: think about this.
    //My guess is that when we transfer huge data (jpg) this will be nessesary
    private void waitForData() {
        Log.d(TAG, "waitForData: WAIT");
        oldDataSize = data.size();
        new Timer().schedule(new TimerTask() {
            public void run() {
                checkDataSize();
            }
        }, 500);
    }

    private void checkDataSize() {
        Log.d(TAG, "checkDataSize: CHECK");
        if(data.size() > oldDataSize) {
            waitForData();
        } else {
            process();
            data.reset();
        }
    }

    private void process() {
        Log.d(TAG, "process: data size: " + data.size());
        Log.d(TAG, "process: data: " + UartUtils.bytesToHex(data.toByteArray(), data.size()));
        if(data.size() == Message.MSG_SIZE) {
            processResponse();
        } else if(data.size() > Message.MSG_SIZE) {
            processDataPacket();
        } else {
            //wtf?
            Log.e(TAG, "process: saw package of strange size.");
        }
    }

    private void processResponse() {
        Response rsp = new Response();
        rsp.addBytes(data.toByteArray());
        checkResponse(rsp);
    }

    private void processDataPacket() {
        Response rsp = new Response();
        DataPacket dp;
        rsp.addBytes(Arrays.copyOfRange(data.toByteArray(), 0, Message.MSG_SIZE - 1));
        checkResponse(rsp);
        if((data.size() - Message.MSG_SIZE) == DataPacket.MOD_INFO_SIZE) {
            dp = new DataPacket(DataPacket.MOD_INFO_SIZE);
            dp.addBytes(Arrays.copyOfRange(data.toByteArray(), Message.MSG_SIZE, (DataPacket.MOD_INFO_SIZE + Message.MSG_SIZE)));
            Log.i(TAG, "processDataPacket: Got Info: " + dp.getInfo());
        } else {
            Log.e(TAG, "processDataPacket: Unknown Data Size. Could not create DataPacket.");
        }
    }

    private void checkResponse(Response rsp) {
        if(!rsp.isEmpty()) {
            Log.i(TAG, "checkResponse: Got reponse: " + rsp.toString());
            if(!rsp.getAck()) {
                Log.e(TAG, "checkResponse: NACK!");
                Log.d(TAG, "checkResponse: ERROR TEXT: " + rsp.getError());
            } else {
                Log.d(TAG, "checkResponse: ACK!");
            }
        }
    }
}