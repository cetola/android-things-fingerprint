package edu.pdx.ekbotecetolafinalpi.managers;

import java.util.List;

import edu.pdx.ekbotecetolafinalpi.account.DeviceInfo;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

public interface UartManager {
    List<String> getDeviceList();
    int openUsbUart(String name);
    int sendCommand(Command cmd);
    DeviceInfo getDeviceInfo();
    Response getResponse();
}
