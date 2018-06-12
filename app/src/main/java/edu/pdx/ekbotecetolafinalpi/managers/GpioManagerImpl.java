package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Implementation of the GPIO manager. Currently only unlocks a box (actuates a solenoid).
 */
public class GpioManagerImpl implements GpioManager {

    private static final String TAG = "GpioManagerImpl";
    private PeripheralManager manager;
    private Gpio gpio;

    public GpioManagerImpl() {
        try {
            setupManager();
            activateGpio();
        } catch (Exception e) {
            Log.e(TAG, "Bad JooJoo. Closing IO.");
            closeIO();
        }
    }

    /**
     * Start the GPIO low. It will go high when needed.
     * @throws IOException for failure to connect to the GPIO pin
     */
    private void activateGpio() throws IOException {
        try {
            Log.d(TAG, "activateGpio: ========================================GPIO LOW");
            gpio = manager.openGpio("BCM4");
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            Log.i(TAG, "GPIO Ready");

        } catch (IOException e) {
            Log.e(TAG, "Failed to activate BCM4.");
            throw e;
        }
    }

    /**
     * Get a local instance of the Android Things PeripheralManager.
     * @throws Exception I'm not sure if google has released the source code for android.things
     * classes yet. I'm sure the getInstance method will throw some kind of error. This catches it.
     */
    private void setupManager() throws Exception {
        try {
            manager = PeripheralManager.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup PeripheralManager.");
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            throw new Exception("setupManger failed");
        }

    }

    /**
     * Someday we might actually want to exit the program cleanly, so close the IOs we opened.
     */
    private void closeIO() {
        try {
            gpio.setValue(false);
        } catch (IOException e) {
            Log.e(TAG, "closeIO: Error on PeripheralIO API");
        }
    }
    
    @Override
    /**
     * Actuate the solenoid, then wait 1 second, and bring the GPIO low again.
     */
    public void unlockBox() {
        try {
            Log.d(TAG, "unlockBox: ========================================UNLOCK");
            gpio.setValue(true);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    try {
                        gpio.setValue(false);
                    } catch (IOException e) {
                        Log.e(TAG, "unlockBox false: IO failure", e);
                    }
                }
            }, 1000);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "unlockBox true: IO failure", e);
        }
    }
}
