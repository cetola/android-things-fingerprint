package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import edu.pdx.ekbotecetolafinalpi.dao.DeviceDao;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDaoImpl;
import edu.pdx.ekbotecetolafinalpi.states.FingerState;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandMap;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

public abstract class FiniteStateMachineManager {

    private static final String TAG = "FiniteStateMachineManag";
    protected UartManager uartManager;
    protected FirestoreManager dbManager;
    protected DeviceDao deviceDao;
    protected int state = FingerState.NOT_ACTIVE;
    protected int nextState = FingerState.NOT_ACTIVE;
    protected int attempts;
    protected static final int MAX_ATTEMPTS = 4;

    public FiniteStateMachineManager(UartManager uartManager, FirestoreManager dbManager) {
        this.uartManager = uartManager;
        this.dbManager = dbManager;
        this.uartManager = uartManager;
        deviceDao = new DeviceDaoImpl();
        attempts = 0;
        this.uartManager.setResponseListener(new UartManager.ResponseReadyListener() {
            @Override
            public void onResponseReady(Response rsp) {
                if(!rsp.isEmpty()) {
                    if(!rsp.getAck()) {
                        doNack(rsp);
                    } else {
                        doAck(rsp);
                    }
                }
            }
        });
    }

    abstract protected void startStateMachine();
    abstract void doAck(Response rsp);
    abstract void doNack(Response rsp);

    protected void stopStateMachine() {
        deviceDao.sendMessage("Stopping the state machine.");
        state = FingerState.NOT_ACTIVE;
        nextState = FingerState.NOT_ACTIVE;
        uartManager.toggleLed(uartManager.LED_OFF);
    }

    protected void sendCommand(Command cmd) {
        uartManager.queueCommand(cmd);
    }
    protected void getFingerPress() {
        attempts++;
        if(attempts < MAX_ATTEMPTS) {
            deviceDao.sendMessage("Missed your finger, try again.");
            sendCommand(new Command(0, CommandMap.IsPressFinger));
        } else {
            deviceDao.sendMessage("Sorry, missed finger too much. Please start over.");
            stopStateMachine();
            attempts = 0;
        }
    }


    protected void error() {
        Log.e(TAG, "error: got ack in enroll state: " + state);
    }

    /**
     *
     * @return True for acitve, false for inactive
     */
    public boolean isActive() {
        return !(state == FingerState.NOT_ACTIVE);
    }
}
