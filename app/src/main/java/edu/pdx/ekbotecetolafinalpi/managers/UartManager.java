package edu.pdx.ekbotecetolafinalpi.managers;

import java.util.List;

import edu.pdx.ekbotecetolafinalpi.account.DeviceInfo;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

public interface UartManager {
    List<String> getDeviceList();
    int openUsbUart(String name);
    int queueCommand(Command cmd);
    void getDeviceInfo();
    void setResponseListener(ResponseReadyListener listener);
    void setDeviceInfoReadyListener(DeviceInfoReadyListener listener);

    interface ResponseReadyListener {
        void onResponseReady(Response response);
    }

    interface DeviceInfoReadyListener {
        void onDeviceInfoReady(DeviceInfo info);
    }
}
