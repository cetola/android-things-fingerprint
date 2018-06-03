package edu.pdx.ekbotecetolafinalpi.uart;

import android.util.Log;

import com.google.android.things.pio.UartDevice;

import java.io.IOException;

public class Command extends Message {

    private static final String TAG = "Command";
    private static final int PARAM_OFFSET = 4;
    private static final int PARAM_LENGTH = 4;
    private static final int CMD_OFFSET = 8;
    private static final int CHKSUM_OFFSET = 10;

    private byte[] params;
    private char cmd;
    private char checksum;

    public Command() {
        super();
        params = new byte[4];
        getData().putChar(Message.START_CODE);
        getData().putChar(Message.DEVICE_ID);
    }

    public void setParams(int in) {
        params[0] = (byte)(in & 0x000000ff);
        params[1] = (byte)((in & 0x0000ff00) >> 8);
        params[2] = (byte)((in & 0x00ff0000) >> 16);
        params[3] = (byte)((in & 0xff000000) >> 24);
        for(int i=0; i < PARAM_LENGTH; i++) {
            getData().put(PARAM_OFFSET + i, params[i]);
        }
    }

    public void setCmd(char c) {
        cmd = c;
        getData().putChar(CMD_OFFSET, cmd);
    }

    public void setChecksum() {
        checksum = 0;
        for(int i = 0; i < 10; i++) {
            checksum += getData().get(i) & 0xff;
        }
        getData().putChar(CHKSUM_OFFSET, checksum);
    }
}
