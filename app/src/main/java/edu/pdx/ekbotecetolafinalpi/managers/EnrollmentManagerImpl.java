package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandList;
import edu.pdx.ekbotecetolafinalpi.uart.UartUtils;

public class EnrollmentManagerImpl implements EnrollmentManager{
    private static final String TAG = "EnrollmentManagerImpl";
    private UartManager uartManager;
    private int count;

    public EnrollmentManagerImpl(UartManager uartManager) {
        this.uartManager = uartManager;
    }

    public int getEnrollmentCount() {
        UartUtils.holdOnASec(1);
        uartManager.queueCommand(new Command(0, CommandList.GetEnrollCount));
        UartUtils.holdOnASec(1);
        count = uartManager.getResponse().getParams();
        return count;
    }

}
