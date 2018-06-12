package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import edu.pdx.ekbotecetolafinalpi.account.History;
import edu.pdx.ekbotecetolafinalpi.account.User;
import edu.pdx.ekbotecetolafinalpi.dao.HistoryDao;
import edu.pdx.ekbotecetolafinalpi.dao.HistoryDaoImpl;
import edu.pdx.ekbotecetolafinalpi.realtime.UnlockStatus;
import edu.pdx.ekbotecetolafinalpi.states.IdentificationState;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandMap;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

/**
 * Implements the "identification" tasks as a Finite State Machine (FSM). Uses the list of states
 * from the {@link IdentificationState} object. Since there is no "clock" we use the "ACK" or "NACK"
 * of the fingerprint scanner {@link Response} as the clock.
 */
public class IdentificationManagerImpl extends FiniteStateMachineManager implements IdentificationManager {
    private static final String TAG = "IdentificationManagerIm";
    private HistoryDao historyDao;
    private GpioManager gpioManager;

    public IdentificationManagerImpl(UartManager uartManager, FirestoreManager dbManager, GpioManager gpioManager) {
        super(uartManager, dbManager);
        historyDao = new HistoryDaoImpl(dbManager);
        this.gpioManager = gpioManager;
    }

    @Override
    protected void startStateMachine() {
        Log.d(TAG, "startStateMachine");
        state = IdentificationState.LED_ON;
        nextState = IdentificationState.FINGER_PRESS;
        uartManager.toggleLed(uartManager.LED_ON);
    }

    @Override
    void doAck(Response rsp) {
        switch (state) {
            case IdentificationState.NOT_ACTIVE:
                //do nothing if this response is not meant for us.
                break;
            default:
                step(rsp);
                break;
        }
    }

    @Override
    void doNack(Response rsp) {
        switch (state) {
            case IdentificationState.NOT_ACTIVE:
                //do nothing
                break;
            case IdentificationState.IDENT_START:
                Log.d(TAG, "Not authorized.");
                deviceDao.setUnlockStatus(UnlockStatus.FAIL);
                stopStateMachine();
                break;
            default:
                deviceDao.setUnlockStatus(UnlockStatus.FAIL);
                Log.d(TAG, "Error IDENT on state: " + state);
                Log.d(TAG, "Error: " + rsp.getError());
                stopStateMachine();
                break;
        }
    }

    /**
     * Stop through the process of identifying a user and triggering the lock.
     * @param rsp
     */
    private void step(Response rsp) {
        switch (state) {
            case IdentificationState.LED_ON:
                deviceDao.setUnlockStatus(UnlockStatus.PROCESS);
                state = nextState;
                nextState = IdentificationState.CAPTURE_FINGER;
                uartManager.queueCommand(new Command(0, CommandMap.IsPressFinger));
                break;
            case IdentificationState.FINGER_PRESS:
                if(rsp.getParams() > 0) {
                    if(!getFingerPress()) {
                        deviceDao.setUnlockStatus(UnlockStatus.FAIL);
                    }
                } else {
                    state = nextState;
                    nextState = IdentificationState.IDENT_START;
                    deviceDao.setUnlockStatus(UnlockStatus.PROCESS);
                    sendCommand(new Command(0, CommandMap.CaptureFinger));
                }
                break;
            case IdentificationState.CAPTURE_FINGER:
                state = nextState;
                nextState = IdentificationState.NOT_ACTIVE;
                deviceDao.setUnlockStatus(UnlockStatus.PROCESS);
                sendCommand(new Command(0, CommandMap.Identify1_N));
                break;
            case IdentificationState.IDENT_START:
                Log.d(TAG, "Finger Identified as Scanner ID: " + rsp.getParams());
                stopStateMachine();
                historyDao.saveHistory(new History(currentUser.getUsername()), new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "saveHistory: onSuccess: saved history " + documentReference.getId());
                    }
                });
                gpioManager.unlockBox();
                deviceDao.setUnlockStatus(UnlockStatus.UNLOCKED);
                break;
            default:
                Log.d(TAG, "step: " + state);
                break;
        }
    }

    /**
     * If the user is found in the datastore, set the current user.
     * @param userId
     */
    @Override
    public void identifyFinger(final String userId) {
        userDao.getUserById(userId, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    User u = documentSnapshot.toObject(User.class);
                    u.setId(documentSnapshot.getId());
                    setCurrentUser(u);
                }
            }
        });
    }

    /**
     * Sets the current user and starts the state machine.
     * @param u
     */
    private void setCurrentUser(User u) {
        this.currentUser = u;
        startStateMachine();
    }
}
