package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import edu.pdx.ekbotecetolafinalpi.account.Enrollment;
import edu.pdx.ekbotecetolafinalpi.account.EnrollmentStep;
import edu.pdx.ekbotecetolafinalpi.account.User;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDao;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDaoImpl;
import edu.pdx.ekbotecetolafinalpi.dao.EnrollmentDao;
import edu.pdx.ekbotecetolafinalpi.dao.EnrollmentDaoImpl;
import edu.pdx.ekbotecetolafinalpi.dao.UserDao;
import edu.pdx.ekbotecetolafinalpi.dao.UserDaoImpl;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandMap;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

public class EnrollmentManagerImpl implements EnrollmentManager {
    private static final String TAG = "EnrollmentManagerImpl";
    private UartManager uartManager;
    private DeviceDao deviceDao;
    private int enrollState;
    private int nextState;
    private int attempts;
    private int enrollNumber;
    private int currentFingerId;
    private int currentScannerId;
    private Enrollment currentEnrollment;
    private User currentUser;
    private EnrollmentDao enrollmentDao;
    private UserDao userDao;

    public EnrollmentManagerImpl(UartManager uartManager, FirestoreManager dbManager) {
        enrollState = EnrollmentStep.NOT_ENROLLING;
        deviceDao = new DeviceDaoImpl();
        userDao = new UserDaoImpl(dbManager);
        enrollmentDao = new EnrollmentDaoImpl(dbManager);
        attempts = 0;
        this.uartManager = uartManager;
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

    private void doAck(Response rsp) {
        switch (enrollState) {
            case EnrollmentStep.NOT_ENROLLING:
                //do nothing
                break;
            case EnrollmentStep.ENROLL_COUNT:
                showCount(rsp);
                break;
            case EnrollmentStep.CHECK_ENROLLED:
                showEnrollStatus(rsp);
                break;
            case EnrollmentStep.ENROLL_FAIL:
                error();
                break;
            case EnrollmentStep.DELETE_ALL:
                Log.d(TAG, "=========================DELETE ALL SUCCESS. Hope you meant it.");
                enrollState = EnrollmentStep.NOT_ENROLLING;
                break;
            default:
                step(rsp);
                break;
        }
    }

    private void doNack(Response rsp) {
        switch (enrollState) {
            case EnrollmentStep.CHECK_ENROLLED:
                showEnrollStatus(rsp);
                break;
            case EnrollmentStep.ENROLL_3:
                deviceDao.sendMessage("This finger is already enrolled at ID" + rsp.getParams() + ".");
                enrollFail();
                break;
            case EnrollmentStep.DELETE_ALL:
                Log.d(TAG, "=========================DELETE ALL FAILED: " + rsp.getError());
                enrollState = EnrollmentStep.NOT_ENROLLING;
                break;
            default:
                enrollState = EnrollmentStep.NOT_ENROLLING;
                deviceDao.sendMessage("Error enrolling on: " + enrollNumber);
                deviceDao.sendMessage("Error: " + rsp.getError());
                uartManager.queueCommand(new Command(0, CommandMap.CmosLed));
                break;
        }
    }

    private void getEnrollCount() {
        this.enrollState = EnrollmentStep.ENROLL_COUNT;
        uartManager.queueCommand(new Command(0, CommandMap.GetEnrollCount));
    }

    private void getEnrollStatus() {
        this.enrollState = EnrollmentStep.CHECK_ENROLLED;
        uartManager.queueCommand(new Command(currentScannerId, CommandMap.CheckEnrolled));
    }

    private void step(Response rsp) {
        switch (enrollState) {
            case EnrollmentStep.LED_ON:
                enrollState = nextState;
                nextState = EnrollmentStep.FINGER_PRESS;
                sendEnrollCommand();
                break;
            case EnrollmentStep.FINGER_PRESS:
                if(rsp.getParams() > 0) {
                    getFingerPress();
                } else {
                    enrollState = nextState;
                    nextState = enrollNumber;
                    deviceDao.sendMessage("Capturing finger image...");
                    sendCommand(new Command(0, CommandMap.CaptureFinger));
                }
                break;
            case EnrollmentStep.ENROLL_START:
                attempts = 0;
                deviceDao.sendMessage("Please press finger (1st template)");
                getNextTemplate();
                break;
            case EnrollmentStep.ENROLL_1:
                attempts = 0;
                deviceDao.sendMessage("Please press finger (2nd template)");
                getNextTemplate();
                break;
            case EnrollmentStep.ENROLL_2:
                attempts = 0;
                deviceDao.sendMessage("Please press finger (3rd template)");
                getNextTemplate();
                break;
            case EnrollmentStep.ENROLL_3:
                attempts = 0;
                deviceDao.sendMessage("Enrolled ID " + currentScannerId + " successfully.");
                saveEnrollment();
                break;
            case EnrollmentStep.CAPTURE_FINGER:
                enrollState = nextState;
                nextState = EnrollmentStep.FINGER_PRESS;
                sendEnrollCommand();
                break;
        }
    }

    private void saveEnrollment() {
        enrollState = EnrollmentStep.NOT_ENROLLING;
        nextState = EnrollmentStep.NOT_ENROLLING;
        uartManager.queueCommand(new Command(0, CommandMap.CmosLed));
        enrollmentDao.saveEnrollment(currentEnrollment, new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "onSuccess: Saved Enrollment: " + documentReference.getId());
            }
        });
    }

    private void getNextTemplate() {
        enrollState = nextState;
        nextState = EnrollmentStep.CAPTURE_FINGER;
        deviceDao.sendMessage("Checking for finger...");
        sendCommand(new Command(0, CommandMap.IsPressFinger));
    }

    private void sendEnrollCommand() {
        switch (enrollNumber) {
            case EnrollmentStep.ENROLL_START:
                deviceDao.sendMessage("EnrollmentStep start.");
                sendCommand(new Command(currentScannerId, CommandMap.EnrollStart));
                break;
            case EnrollmentStep.ENROLL_1:
                deviceDao.sendMessage("Enroll 1. Please remove your finger.");
                sendCommand(new Command(currentScannerId, CommandMap.Enroll1));
                break;
            case EnrollmentStep.ENROLL_2:
                deviceDao.sendMessage("Enroll 2. Please remove your finger.");
                sendCommand(new Command(currentScannerId, CommandMap.Enroll2));
                break;
            case EnrollmentStep.ENROLL_3:
                deviceDao.sendMessage("Enroll 3. Please remove your finger.");
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
            return EnrollmentStep.LED_OFF;
        }
    }

    private void getFingerPress() {
        attempts++;
        if(attempts < 4) {
            deviceDao.sendMessage("Missed your finger, try again.");
            sendCommand(new Command(0, CommandMap.IsPressFinger));
        } else {
            deviceDao.sendMessage("Sorry, please start over.");
            enrollFail();
            attempts = 0;
        }
    }

    private void error() {
        Log.e(TAG, "error: got ack in enroll state: " + enrollState);
    }

    private void enrollFail() {
        Log.e(TAG, "enrollFail: state: " + enrollState);
        enrollState = EnrollmentStep.NOT_ENROLLING;
        uartManager.queueCommand(new Command(0, CommandMap.CmosLed));
    }

    private void sendCommand(Command cmd) {
        uartManager.queueCommand(cmd);
    }

    private void getEnrollmentCount() {
        uartManager.queueCommand(new Command(0, CommandMap.GetEnrollCount));
    }

    private void showCount(Response rsp) {
        deviceDao.sendMessage("Got count: " + rsp.getParams());
        getEnrollStatus();
    }

    private void showEnrollStatus(Response rsp) {
        if(!rsp.getAck()) {
            deviceDao.sendMessage("Scanner ID not enrolled, enrolling...");
            startEnrollment();
        } else {
            deviceDao.sendMessage("ID " + currentScannerId + " already enrolled.");
            enrollState = EnrollmentStep.NOT_ENROLLING;
            uartManager.queueCommand(new Command(0, CommandMap.CmosLed));
        }
    }

    private void startEnrollment() {
        currentEnrollment = new Enrollment(currentScannerId, currentFingerId, currentUser,
                userDao.getUserRef(currentUser.getId()));
        deviceDao.sendMessage("Starting enrollment, LED ON");
        enrollState = EnrollmentStep.LED_ON;
        nextState = EnrollmentStep.ENROLL_START;
        enrollNumber = EnrollmentStep.ENROLL_START;
        uartManager.queueCommand(new Command(1, CommandMap.CmosLed));
    }

    @Override
    public void checkEnroll(int scannerId, int finger, final String userId) {
        Log.d(TAG, "checkEnroll: userId: " + userId);
        currentFingerId = finger;
        currentScannerId = scannerId;
        //TODO: lock
        userDao.getUserById(userId, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    //TODO: not sure why the ID doesn't get returned on the doc snapshot
                    User u = documentSnapshot.toObject(User.class);
                    u.setId(userId);
                    enrollUser(u);
                }
            }
        });
    }

    private void enrollUser(User user) {
        Log.d(TAG, "enrollUser: " + user.getUsername());
        currentUser = user;
        getEnrollStatus();
    }

    public void deleteAll() {
        enrollState = EnrollmentStep.DELETE_ALL;
        uartManager.queueCommand(new Command(1, CommandMap.DeleteAll));
    }
}
