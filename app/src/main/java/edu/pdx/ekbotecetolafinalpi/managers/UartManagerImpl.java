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
import edu.pdx.ekbotecetolafinalpi.uart.CommandMap;
import edu.pdx.ekbotecetolafinalpi.uart.CommandQueue;
import edu.pdx.ekbotecetolafinalpi.uart.DataPacket;
import edu.pdx.ekbotecetolafinalpi.uart.Message;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

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
    private boolean waiting = false;
    private int waitCount = 0;
    private int oldDataSize;
    CommandQueue q;
    DeviceInfo info;
    private ResponseReadyListener responseReadyListener;
    private DeviceInfoReadyListener deviceInfoReadyListener;

    public UartManagerImpl(FirestoreManager dbManager) {
        pm = PeripheralManager.getInstance();
        q = new CommandQueue(dbManager);

        new Timer().schedule(new TimerTask() {
            public void run() {
                processQueue();
            }
        }, 0, 2000);
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
            oldDataSize = data.size();
            waiting = true;
            new Timer().schedule(new TimerTask() {
                public void run() {
                    checkDataSize();
                }
            }, 500);
        } else {
            //Log.d(TAG, "waitForData: Already waiting: " + waitCount);
        }
    }

    private void checkDataSize() {
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
        recievedResponse(rsp);
    }

    private void processDataPacket() {
        Response rsp = new Response();
        rsp.addRangeBytes(data.toByteArray(), 0, Message.MSG_SIZE - 1);
        recievedResponse(rsp);
        if(isDeviceInfo()) {
            setDeviceInfo();
        } else if (isEnrollData()) {
            Log.i(TAG, "processDataPacket: got enroll data");
        }else {
            Log.e(TAG, "processDataPacket: Unknown Data Size: " + data.size() + ". Could not create DataPacket.");
        }
    }

    private boolean isDeviceInfo() {
        return (data.size() - Message.MSG_SIZE) == DataPacket.MOD_INFO_SIZE;
    }

    private boolean isEnrollData() {
        return (data.size() - Message.MSG_SIZE) == DataPacket.ENROLL_SIZE;
    }

    private void setDeviceInfo() {
        DataPacket dp;
        dp = new DataPacket(DataPacket.MOD_INFO_SIZE);
        dp.addRangeBytes(data.toByteArray(), Message.MSG_SIZE, (DataPacket.MOD_INFO_SIZE + Message.MSG_SIZE));
        info = dp.getDeviceInfo();
        deviceInfoReadyListener.onDeviceInfoReady(info);
    }

    public void getDeviceInfo() {
        queueCommand(new Command(1, CommandMap.Open));
    }

    private void recievedResponse(Response rsp) {
        q.addResponse(rsp);
        responseReadyListener.onResponseReady(rsp);
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
            // Read any initially buffered data
            getInputHandler().post(uartRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open UART device", e);

        }
        return 0;
    }

    @Override
    public int queueCommand(Command cmd) {
        q.addCommand(cmd);
        return q.getSize();
    }

    private void processQueue() {
        if(q.getSize() > 0 && !waiting) {
            sendCommand(q.getNextCommand());
        } else {
            //Log.d(TAG, "processQueue: wait: " + waiting + " queue size " + q.getSize());
        }
    }

    private int sendCommand(Command cmd) {
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

    public void setResponseListener(ResponseReadyListener rspListener) {
        this.responseReadyListener = rspListener;
    }

    public void setDeviceInfoReadyListener(DeviceInfoReadyListener deviceInfoListener) {
        this.deviceInfoReadyListener = deviceInfoListener;
    }
}
