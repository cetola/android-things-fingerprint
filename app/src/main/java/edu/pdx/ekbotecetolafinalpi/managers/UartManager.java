package edu.pdx.ekbotecetolafinalpi.managers;

import java.util.List;

import edu.pdx.ekbotecetolafinalpi.account.DeviceInfo;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandMap;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

/**
 * The UartManager abstracts out the idea of an Android Things Uart Device.
 */
public interface UartManager {

    //convenience static variables for toggling the LED
    int LED_OFF = 0;
    int LED_ON = 1;

    /**
     * Get the list of uart devices available on the device.
     * @return An array list of UART device name strings.
     */
    List<String> getDeviceList();

    /**
     * Open the USB UART with the given name. In theory would work for any UART.
     * @param name the name of the UART
     * @return 0 = success, anything else = error
     */
    int openUsbUart(String name);

    /**
     * Queue a command to be sent to the open UART.
     * Does not check to be sure a UART has been opened.
     * @param cmd the command to send
     * @return the current queue size
     */
    int queueCommand(Command cmd);

    /**
     * Get the device info for the scanner.
     * Runs the command {@link CommandMap}.Open with "1" as the parameter.
     */
    void getDeviceInfo();

    /**
     * Sets the "Response" listener. This will be called when data is in the UART's buffer.
     * @param listener
     */
    void setResponseListener(ResponseReadyListener listener);

    /**
     * Sets the device info listener. This will only be fired once after the "get info" command is
     * sent.
     * @param listener
     */
    void setDeviceInfoReadyListener(DeviceInfoReadyListener listener);

    /**
     * Toggles the LED of the Fingerprint Scanner. This is a convenience method and should probably
     * be abstracted out to a "FingerprintScanner" subclass once there are enough of these kinds of
     * methods.
     * @param onOff
     */
    void toggleLed(int onOff);

    //LISTENERS
    interface ResponseReadyListener {
        void onResponseReady(Response response);
    }

    interface DeviceInfoReadyListener {
        void onDeviceInfoReady(DeviceInfo info);
    }
}
