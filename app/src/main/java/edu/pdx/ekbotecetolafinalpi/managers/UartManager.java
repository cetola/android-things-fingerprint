package edu.pdx.ekbotecetolafinalpi.managers;

import java.util.List;

import edu.pdx.ekbotecetolafinalpi.uart.Command;

public interface UartManager {
    List<String> getDeviceList();
    int openUsbUart(String name);
    int sendCommand(Command cmd);
}
