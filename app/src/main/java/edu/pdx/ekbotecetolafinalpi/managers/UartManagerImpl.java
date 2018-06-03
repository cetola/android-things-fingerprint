package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.pdx.ekbotecetolafinalpi.account.DeviceInfo;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.DataPacket;
import edu.pdx.ekbotecetolafinalpi.uart.Message;
import edu.pdx.ekbotecetolafinalpi.uart.Response;
import edu.pdx.ekbotecetolafinalpi.uart.UartUtils;

public class UartManagerImpl extends ThreadedManager implements UartManager {
    private static final String TAG = "UartManagerImpl";
    private UartDevice uartDevice;
    private int baudRate = 9600;
    private int dataBits = 8;
    private int stopBits = 1;
    private int parity = UartDevice.PARITY_NONE;
    private PeripheralManager pm;
    private static final int CHUNK_SIZE = 512;
    private ByteArrayOutputStream data = new ByteArrayOutputStream();
    //TODO: not sure if these need to be volatile
    public volatile boolean waiting = false;
    public volatile int waitCount = 0;
    private int oldDataSize;
    private DeviceInfo info;
    private Response response;

    public UartManagerImpl() {
        super();
        pm = PeripheralManager.getInstance();
    }

    /**
     * Callback invoked when UART receives new incoming data.
     */
    private UartDeviceCallback callback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            readData();
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    private Runnable uartRunnable = new Runnable() {
        @Override
        public void run() {
            readData();
        }
    };

    //TODO: using a ByteArrayOutputStream in a thread, is this wise?
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
    //My guess is that when we transfer huge data (jpg) this will be necessary
    private void waitForData() {
        waitCount++;
        if(!waiting) {
            Log.d(TAG, "waitForData: WAIT");
            oldDataSize = data.size();
            waiting = true;
            new Timer().schedule(new TimerTask() {
                public void run() {
                    checkDataSize();
                }
            }, 500);
        } else {
            Log.d(TAG, "waitForData: Already waiting: " + waitCount);
        }
    }

    private void checkDataSize() {
        Log.d(TAG, "checkDataSize: CHECK");
        if(data.size() > oldDataSize) {
            waiting = false;
            waitCount = 0;
            waitForData();
        } else {
            process();
            data.reset();
            waiting = false;
            waitCount = 0;
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
        rsp.addRangeBytes(data.toByteArray(), 0, Message.MSG_SIZE - 1);
        checkResponse(rsp);
        if(isDeviceInfo()) {
            setDeviceInfo();
        } else {
            Log.e(TAG, "processDataPacket: Unknown Data Size: " + data.size() + ". Could not create DataPacket.");
        }
    }

    private boolean isDeviceInfo() {
        return (data.size() - Message.MSG_SIZE) == DataPacket.MOD_INFO_SIZE;
    }

    private void setDeviceInfo() {
        DataPacket dp;
        dp = new DataPacket(DataPacket.MOD_INFO_SIZE);
        dp.addRangeBytes(data.toByteArray(), Message.MSG_SIZE, (DataPacket.MOD_INFO_SIZE + Message.MSG_SIZE));
        info = dp.getDeviceInfo();
    }

    public DeviceInfo getDeviceInfo() {
        return this.info;
    }

    private void checkResponse(Response rsp) {
        if(!rsp.isEmpty()) {
            Log.i(TAG, "checkResponse: Got reponse: " + rsp.toString());
            if(!rsp.getAck()) {
                Log.e(TAG, "checkResponse: NACK!");
                Log.d(TAG, "checkResponse: ERROR TEXT: " + rsp.getError());
            } else {
                Log.d(TAG, "checkResponse: ACK!");
                Log.d(TAG, "checkResponse: params: " + rsp.getParams());
            }
        }
        response = rsp;
    }

    public Response getResponse() {
        return this.response;
    }

    @Override
    public List<String> getDeviceList() {
        List<String> deviceList = pm.getUartDeviceList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No UART port available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);

        }
        return deviceList;
    }


    @Override
    public int openUsbUart(String name) {
        createLooperThread();
        try {
            openUart(name);
            getInputHandler().post(uartRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open UART device", e);

        }
        return 0;
    }

    @Override
    public int sendCommand(Command cmd) {
        cmd.setChecksum();
        Log.i(TAG, "sendCommand: sending data: " + UartUtils.bytesToHex(cmd.getData().array(), cmd.getData().array().length));
        try {
            uartDevice.write(cmd.getData().array(), cmd.getData().array().length);
        } catch (IOException e) {
            Log.e(TAG, "sendCommand: unable to send command.", e);
        }
        return 0;
    }

    /**
     * Access and configure the requested UART device for 8N1.
     *
     * @param name Name of the UART peripheral device to open.
     *
     * @throws IOException if an error occurs opening the UART port.
     */
    private void openUart(String name) throws IOException {
        Log.d(TAG, "openUart: opening " + name);
        uartDevice = PeripheralManager.getInstance().openUartDevice(name);
        uartDevice.setBaudrate(baudRate);
        uartDevice.setDataSize(dataBits);
        uartDevice.setParity(parity);
        uartDevice.setStopBits(stopBits);
        uartDevice.setHardwareFlowControl(UartDevice.HW_FLOW_CONTROL_NONE);

        uartDevice.registerUartDeviceCallback(getInputHandler(), callback);
    }
}
