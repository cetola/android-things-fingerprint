package edu.pdx.ekbotecetolafinalpi.uart;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@IgnoreExtraProperties
public class Message {
    private static final String TAG = "Message";
    public static final int MSG_SIZE = 12;
    public static final char START_CODE = 0xAA55;
    public static final char DEVICE_ID = 0x0001;
    private static final int PARAM_OFFSET = 4;
    private static final int PARAM_LENGTH = 4;
    private byte[] params;
    private String id;
    private ByteBuffer data;
    private String strData;
    private Timestamp created;

    public Message() {
        params = new byte[4];
        data = ByteBuffer.allocate(MSG_SIZE);
        this.data.order(ByteOrder.LITTLE_ENDIAN);
    }

    public Message(int size) {
        data = ByteBuffer.allocate(size);
        this.data.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void addBytes(byte[] d) {
        Log.d(TAG, "addBytes: " + UartUtils.bytesToHex(d, d.length));
        data.put(d);
        updateStringData();
    }

    public void addRangeBytes(byte[] d, int start, int stop) {
        addBytes(Arrays.copyOfRange(d, start, stop));
    }

    @Exclude
    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
        updateStringData();
    }

    @Override
    public String toString() {
        return UartUtils.bytesToHex(data.array(), data.array().length);
    }

    public void setParams(int in) {
        params[0] = (byte)(in & 0x000000ff);
        params[1] = (byte)((in & 0x0000ff00) >> 8);
        params[2] = (byte)((in & 0x00ff0000) >> 16);
        params[3] = (byte)((in & 0xff000000) >> 24);
        for(int i=0; i < PARAM_LENGTH; i++) {
            getData().put(PARAM_OFFSET + i, params[i]);
        }
        updateStringData();
    }

    public int getParams() {
        int rtn = 0;
        rtn = (rtn << 8) + params[3];
        rtn = (rtn << 8) + params[2];
        rtn = (rtn << 8) + params[1];
        rtn = (rtn << 8) + params[0];
        return rtn;
    }

    protected void updateStringData() {
        this.strData = UartUtils.bytesToHex(data.array(), data.array().length);
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStrData() {
        return strData;
    }

    public void setStrData(String strData) {
        this.strData = strData;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }
}
