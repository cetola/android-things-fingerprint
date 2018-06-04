package edu.pdx.ekbotecetolafinalpi.uart;

import android.util.Log;

import edu.pdx.ekbotecetolafinalpi.account.DeviceInfo;

public class DataPacket extends Message {
    private static final String TAG = "DataPacket";
    public static final int MOD_INFO_SIZE = 30;
    private int size;
    private DeviceInfo deviceInfo;
    private String commandId;

    public DataPacket(int size) {
        super(size);
        setSize(size);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getInfo() {
        String rtn = null;
        switch (getSize()) {
            case MOD_INFO_SIZE:
                rtn = getDeviceInfo().toString();
                break;
            default:
                Log.e(TAG, "getInfo: Data Size Unknown. Cannot get info.");
                break;
        }
        return rtn;
    }

    public DeviceInfo getDeviceInfo() {
        if(deviceInfo == null) {
            processData();
        }
        return deviceInfo;
    }

    private void processData() {
        switch (getSize()) {
            case MOD_INFO_SIZE:
                deviceInfo = new DeviceInfo();
                String d = UartUtils.bytesToHex(getData().array(), getData().array().length);
                deviceInfo.setFirmwareVersion(d.substring(DeviceInfo.VERSION_OFFSET, DeviceInfo.ISO_SIZE_OFFSET));
                deviceInfo.setIsoMaxSize(d.substring(DeviceInfo.ISO_SIZE_OFFSET, DeviceInfo.DEVICE_SN_OFFSET));
                deviceInfo.setDeviceSN(d.substring(DeviceInfo.DEVICE_SN_OFFSET));
                break;
            default:
                Log.e(TAG, "processData: Data Size Unknown. Cannot process data.");
        }
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }
}
