package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import edu.pdx.ekbotecetolafinalpi.account.User;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDao;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDaoImpl;
import edu.pdx.ekbotecetolafinalpi.dao.UserDao;
import edu.pdx.ekbotecetolafinalpi.dao.UserDaoImpl;
import edu.pdx.ekbotecetolafinalpi.states.FingerState;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandMap;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

/**
 * Abstracts out the idea of a Finite State Machine (FSM) for use with a fingerprint scanner.
 */
public abstract class FiniteStateMachineManager {

    private static final String TAG = "FiniteStateMachineManager";
    //passed to DAOs rather than being used directly
    @SuppressWarnings("unused")
    private FirestoreManager dbManager;
    protected UartManager uartManager;
    protected DeviceDao deviceDao;
    protected UserDao userDao;
    protected User currentUser;
    protected int state = FingerState.NOT_ACTIVE;
    protected int nextState = FingerState.NOT_ACTIVE;
    protected int attempts;
    protected static final int MAX_ATTEMPTS = 4;

    /**
     * Instanciate the required DAOs and setup a {@link UartManager.ResponseReadyListener} for the
     * messages. Check to see if the fingerprint scanner responsed with ACK or NACK, and call the
     * appropriate method.
     * @param uartManager
     * @param dbManager
     */
    public FiniteStateMachineManager(UartManager uartManager, FirestoreManager dbManager) {
        this.uartManager = uartManager;
        this.dbManager = dbManager;
        this.uartManager = uartManager;
        deviceDao = new DeviceDaoImpl(dbManager);
        userDao = new UserDaoImpl(dbManager);
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

    /**
     * Stops the state machine and turns the LED off.
     */
    protected void stopStateMachine() {
        Log.d(TAG, "Stopping the state machine.");
        state = FingerState.NOT_ACTIVE;
        nextState = FingerState.NOT_ACTIVE;
        uartManager.toggleLed(uartManager.LED_OFF);
    }

    /**
     * Queues a {@link Command} on the {@link UartManager}.
     * @param cmd
     */
    protected void sendCommand(Command cmd) {
        uartManager.queueCommand(cmd);
    }
    protected boolean getFingerPress() {
        attempts++;
        if(attempts < MAX_ATTEMPTS) {
            sendCommand(new Command(0, CommandMap.IsPressFinger));
            return true;
        } else {
            stopStateMachine();
            attempts = 0;
            return false;
        }
    }


    protected void error() {
        Log.e(TAG, "error: got ack in enroll state: " + state);
    }

    /**
     *
     * @return True for active, false for inactive
     */
    public boolean isActive() {
        return !(state == FingerState.NOT_ACTIVE);
    }
}
