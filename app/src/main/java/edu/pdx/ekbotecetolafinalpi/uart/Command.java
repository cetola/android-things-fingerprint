package edu.pdx.ekbotecetolafinalpi.uart;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Command extends Message {

    private static final String TAG = "Command";
    public static final String COLLECTION = "commands";
    private static final int CMD_OFFSET = 8;
    private static final int CHKSUM_OFFSET = 10;
    private String name;

    private char cmd;
    private char checksum;

    public Command(int params, char cmd) {
        super();
        setup();
        setParams(params);
        setCmd(cmd);
        setChecksum();
        updateStringData();
        setCreated(new Timestamp(new Date()));
    }

    private void setup() {
        getData().putChar(Message.START_CODE);
        getData().putChar(Message.DEVICE_ID);
    }

    private void setCmd(char c) {
        cmd = c;
        getData().putChar(CMD_OFFSET, cmd);
    }

    private void setChecksum() {
        checksum = 0;
        for(int i = 0; i < 10; i++) {
            checksum += getData().get(i) & 0xff;
        }
        getData().putChar(CHKSUM_OFFSET, checksum);
    }

    public String getName() {
        return CommandMap.commandList.get(cmd);
    }
}
