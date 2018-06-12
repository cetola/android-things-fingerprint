package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import edu.pdx.ekbotecetolafinalpi.account.Enrollment;
import edu.pdx.ekbotecetolafinalpi.realtime.RegisterFingerprint;
import edu.pdx.ekbotecetolafinalpi.realtime.RegisterFingerprintMsg;
import edu.pdx.ekbotecetolafinalpi.states.EnrollmentState;
import edu.pdx.ekbotecetolafinalpi.account.User;
import edu.pdx.ekbotecetolafinalpi.dao.EnrollmentDao;
import edu.pdx.ekbotecetolafinalpi.dao.EnrollmentDaoImpl;
import edu.pdx.ekbotecetolafinalpi.dao.UserDaoImpl;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandMap;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

/**
 * Implements the "enrollment" tasks as a Finite State Machine (FSM). Uses the list of states
 * from the {@link EnrollmentState} object. Since there is no "clock" we use the "ACK" or "NACK"
 * of the fingerprint scanner {@link Response} as the clock.
 */
public class EnrollmentManagerImpl extends FiniteStateMachineManager implements EnrollmentManager {
    private static final String TAG = "EnrollmentManagerImpl";
    private int enrollNumber;
    private int currentFingerId;
    private int currentScannerId;
    private Enrollment currentEnrollment;
    private EnrollmentDao enrollmentDao;

    public EnrollmentManagerImpl(UartManager uartManager, FirestoreManager dbManager) {
        super(uartManager, dbManager);
        userDao = new UserDaoImpl(dbManager);
        enrollmentDao = new EnrollmentDaoImpl(dbManager);
    }

    public void doAck(Response rsp) {
        switch (state) {
            case EnrollmentState.NOT_ACTIVE:
                //do nothing if we are not currently running
                break;
            case EnrollmentState.CHECK_ENROLLED:
                showEnrollStatus(rsp);
                break;
            case EnrollmentState.ENROLL_FAIL:
                error();
                break;
            case EnrollmentState.DELETE_ALL:
                Log.d(TAG, "DELETE ALL SUCCESS. Hope you meant it.");
                stopStateMachine();
                break;
            default:
                step(rsp);
                break;
        }
    }

    public void doNack(Response rsp) {
        switch (state) {
            case EnrollmentState.CHECK_ENROLLED:
                showEnrollStatus(rsp);
                break;
            case EnrollmentState.ENROLL_3:
                deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.TRY);
                deviceDao.setRegisterFingerprintStatus(RegisterFingerprint.FAILED);
                Log.d(TAG, "This finger is already enrolled at ID" + rsp.getParams() + ".");
                stopStateMachine();
                break;
            case EnrollmentState.DELETE_ALL:
                Log.d(TAG, "DELETE ALL FAILED: " + rsp.getError());
                stopStateMachine();
                break;
            default:
                deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.TRY);
                deviceDao.setRegisterFingerprintStatus(RegisterFingerprint.FAILED);
                Log.d(TAG, "Error enrolling on: " + enrollNumber);
                Log.d(TAG, "Error: " + rsp.getError());
                stopStateMachine();
                break;
        }
    }

    private void getEnrollStatus() {
        this.state = EnrollmentState.CHECK_ENROLLED;
        uartManager.queueCommand(new Command(currentScannerId, CommandMap.CheckEnrolled));
    }

    /**
     * Step through the process of registering a fingerprint with the scanner.
     * Return status as needed using the RegisterFingerprintMsg.
     * @param rsp
     */
    private void step(Response rsp) {
        switch (state) {
            case EnrollmentState.LED_ON:
                state = nextState;
                nextState = EnrollmentState.FINGER_PRESS;
                sendEnrollCommand();
                break;
            case EnrollmentState.FINGER_PRESS:
                if(rsp.getParams() > 0) {
                    getFingerPress();
                } else {
                    state = nextState;
                    nextState = enrollNumber;
                    Log.d(TAG, "Capturing finger image...");
                    sendCommand(new Command(0, CommandMap.CaptureFinger));
                }
                break;
            case EnrollmentState.ENROLL_START:
                attempts = 0;
                deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.PLACE);
                deviceDao.setRegisterFingerprintStatus(RegisterFingerprint.START);
                getNextTemplate();
                break;
            case EnrollmentState.ENROLL_1:
                attempts = 0;
                deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.PLACE);
                getNextTemplate();
                break;
            case EnrollmentState.ENROLL_2:
                attempts = 0;
                deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.PLACE);
                getNextTemplate();
                break;
            case EnrollmentState.ENROLL_3:
                attempts = 0;
                saveEnrollment();
                break;
            case EnrollmentState.CAPTURE_FINGER:
                state = nextState;
                nextState = EnrollmentState.FINGER_PRESS;
                sendEnrollCommand();
                break;
        }
    }

    /**
     * On successful enrollment of a fingerprint, save the enrollment data to the Firestore.
     */
    private void saveEnrollment() {
        stopStateMachine();
        enrollmentDao.saveEnrollment(currentEnrollment, new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "onSuccess: Saved Enrollment: " + documentReference.getId());
            }
        });
        deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.NONE);
        deviceDao.setRegisterFingerprintStatus(RegisterFingerprint.FINISHED);
    }

    /**
     * Starts the process of getting a "fingerprint template". After acquiring 3 templates the
     * scanner will average the three to generate the final template.
     */
    private void getNextTemplate() {
        state = nextState;
        nextState = EnrollmentState.CAPTURE_FINGER;
        deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.PLACE);
        sendCommand(new Command(0, CommandMap.IsPressFinger));
    }

    /**
     * Depending on the number of enroll steps we have completed, we'll need to send the
     * correct command to the sensor.
     */
    private void sendEnrollCommand() {
        switch (enrollNumber) {
            case EnrollmentState.ENROLL_START:
                Log.d(TAG, "EnrollmentState start.");
                sendCommand(new Command(currentScannerId, CommandMap.EnrollStart));
                break;
            case EnrollmentState.ENROLL_1:
                deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.LIFT);
                sendCommand(new Command(currentScannerId, CommandMap.Enroll1));
                break;
            case EnrollmentState.ENROLL_2:
                deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.LIFT);
                sendCommand(new Command(currentScannerId, CommandMap.Enroll2));
                break;
            case EnrollmentState.ENROLL_3:
                deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.LIFT);
                sendCommand(new Command(currentScannerId, CommandMap.Enroll3));
                break;
        }
        enrollNumber = nextEnroll();
    }

    private int nextEnroll() {
        if(enrollNumber < 4) {
            enrollNumber++;
            return enrollNumber;
        } else {
            Log.d(TAG, "nextEnroll: too many enroll steps?");
            return EnrollmentState.LED_OFF;
        }
    }

    /**
     * If the id given is already enrolled, don't try to enroll it again.
     *
     * If not, start the enrollment process.
     * @param rsp
     */
    private void showEnrollStatus(Response rsp) {
        if(!rsp.getAck()) {
            Log.d(TAG, "Scanner ID not enrolled, enrolling...");
            startStateMachine();
        } else {
            Log.d(TAG, "ID " + currentScannerId + " already enrolled.");
            deviceDao.setRegisterFingerprintMsg(RegisterFingerprintMsg.NONE);
            deviceDao.setRegisterFingerprintStatus(RegisterFingerprint.FAILED);
            stopStateMachine();
        }
    }

    protected void startStateMachine() {
        currentEnrollment = new Enrollment(currentScannerId, currentFingerId, currentUser,
                userDao.getUserRef(currentUser.getId()));
        Log.d(TAG, "Starting enrollment, LED ON");

        //Set current and next state
        state = EnrollmentState.LED_ON;
        nextState = EnrollmentState.ENROLL_START;

        enrollNumber = EnrollmentState.ENROLL_START;
        uartManager.toggleLed(uartManager.LED_ON);
    }

    private void enrollUser(User user) {
        Log.d(TAG, "enrollUser: " + user.getUsername());
        currentUser = user;
        getEnrollStatus();
    }

    public void deleteAll() {
        state = EnrollmentState.DELETE_ALL;
        uartManager.queueCommand(new Command(1, CommandMap.DeleteAll));
    }

    @Override
    public void checkEnroll(int scannerId, int finger, final String userId) {
        Log.d(TAG, "checkEnroll: userId: " + userId);
        currentFingerId = finger;
        currentScannerId = scannerId;
        userDao.getUserById(userId, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    User u = documentSnapshot.toObject(User.class);
                    u.setId(documentSnapshot.getId());
                    enrollUser(u);
                }
            }
        });
    }
}
