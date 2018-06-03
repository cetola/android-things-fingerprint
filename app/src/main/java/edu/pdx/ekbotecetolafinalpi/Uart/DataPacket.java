package edu.pdx.ekbotecetolafinalpi.Uart;

import android.util.Log;

public class DataPacket extends Message {
    private static final String TAG = "DataPacket";
    public static final int MOD_INFO_SIZE = 30;
    private int size;
    private final int VERSION_OFFSET = 8;
    private final int ISO_SIZE_OFFSET = 16;
    private final int DEVICE_SN_OFFSET = 24;
    private String firmwareVersion;
    private String isoMaxSize;
    private String deviceSN;

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
        String info = null;
        switch (getSize()) {
            case MOD_INFO_SIZE:
                processData();
                info = "Firmware Version: " + firmwareVersion;
                info += "\nIso Size Max: " + isoMaxSize;
                info += "\nDevice SN: " + deviceSN;
                break;
            default:
                Log.e(TAG, "getInfo: Data Size Unknown. Cannot get info.");
                break;
        }
        return info;
    }

    private void processData() {
        String d = UartUtils.bytesToHex(getData().array(), getData().array().length);
        firmwareVersion = untangleFirmwareVersion(d.substring(VERSION_OFFSET, ISO_SIZE_OFFSET));
        isoMaxSize = d.substring(ISO_SIZE_OFFSET, DEVICE_SN_OFFSET);
        deviceSN = d.substring(DEVICE_SN_OFFSET);
    }

    private String untangleFirmwareVersion(String tangle) {
        char[] chars = tangle.toCharArray();
        return new StringBuilder().append(chars[6]).append(chars[7])
                .append(chars[4]).append(chars[5])
                .append(chars[2]).append(chars[3])
                .append(chars[0]).append(chars[1])
                .toString();
    }
}
