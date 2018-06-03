package edu.pdx.ekbotecetolafinalpi.uart;

import android.util.Log;

import com.google.android.things.pio.UartDevice;

import java.io.IOException;

public class Command extends Message {

    private static final String TAG = "Command";
    private static final int CMD_OFFSET = 8;
    private static final int CHKSUM_OFFSET = 10;

    private char cmd;
    private char checksum;

    public Command(int params, char cmd) {
        super();
        setup();
        setParams(params);
        setCmd(cmd);
    }

    private void setup() {
        getData().putChar(Message.START_CODE);
        getData().putChar(Message.DEVICE_ID);
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
