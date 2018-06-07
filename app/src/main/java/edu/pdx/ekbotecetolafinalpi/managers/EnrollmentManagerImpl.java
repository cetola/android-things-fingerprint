package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import edu.pdx.ekbotecetolafinalpi.account.Enrollment;
import edu.pdx.ekbotecetolafinalpi.account.EnrollmentStep;
import edu.pdx.ekbotecetolafinalpi.account.User;
import edu.pdx.ekbotecetolafinalpi.dao.DeviceDao;
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
    private int enrollId = 3;
    private Enrollment currentEnrollment;

    public EnrollmentManagerImpl(UartManager uartManager, DeviceDao dd) {
        enrollState = EnrollmentStep.NOT_ENROLLING;
        deviceDao = dd;
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

    public void begin() {
        getEnrollCount();
    }

    private void getEnrollCount() {
        this.enrollState = EnrollmentStep.ENROLL_COUNT;
        uartManager.queueCommand(new Command(0, CommandMap.GetEnrollCount));
    }

    private void getEnrollStatus() {
        this.enrollState = EnrollmentStep.CHECK_ENROLLED;
        uartManager.queueCommand(new Command(enrollId, CommandMap.CheckEnrolled));
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
            default:
                Log.d(TAG, "doNack: error: " + rsp.getError());
                Log.d(TAG, "doNack: -----------------enrollState: " + enrollState);
                uartManager.queueCommand(new Command(0, CommandMap.CmosLed));
                break;
        }
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
                deviceDao.sendMessage("Enrolled ID " + enrollId + " successfully.");
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
        //TODO: save enrollment object
        enrollState = EnrollmentStep.NOT_ENROLLING;
        nextState = EnrollmentStep.NOT_ENROLLING;
        uartManager.queueCommand(new Command(0, CommandMap.CmosLed));
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
                sendCommand(new Command(enrollId, CommandMap.EnrollStart));
                break;
            case EnrollmentStep.ENROLL_1:
                deviceDao.sendMessage("Enroll 1. Please remove your finger.");
                sendCommand(new Command(enrollId, CommandMap.Enroll1));
                break;
            case EnrollmentStep.ENROLL_2:
                deviceDao.sendMessage("Enroll 2. Please remove your finger.");
                sendCommand(new Command(enrollId, CommandMap.Enroll2));
                break;
            case EnrollmentStep.ENROLL_3:
                deviceDao.sendMessage("Enroll 3. Please remove your finger.");
                sendCommand(new Command(enrollId, CommandMap.Enroll3));
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

    public void getEnrollmentCount() {
        uartManager.queueCommand(new Command(0, CommandMap.GetEnrollCount));
    }

    public void startEnrollment(int id, User user) {
        this.currentEnrollment = new Enrollment();
        currentEnrollment.setId(id);
        currentEnrollment.setUser(user);
        deviceDao.sendMessage("Starting enrollment, LED ON");
        enrollState = EnrollmentStep.LED_ON;
        nextState = EnrollmentStep.ENROLL_START;
        enrollNumber = EnrollmentStep.ENROLL_START;
        uartManager.queueCommand(new Command(1, CommandMap.CmosLed));
    }

    private void showCount(Response rsp) {
        deviceDao.sendMessage("Got count: " + rsp.getParams());
        this.enrollState = EnrollmentStep.NOT_ENROLLING;
        getEnrollStatus();
    }

    private void showEnrollStatus(Response rsp) {
        deviceDao.sendMessage("Got enroll status ack: " + rsp.getAck());
        deviceDao.sendMessage("Got enroll status params: " + rsp.getParams());
        this.enrollState = EnrollmentStep.NOT_ENROLLING;
        if(!rsp.getAck()) {
            deviceDao.sendMessage("Error code: " + rsp.getError());
            deviceDao.sendMessage("Not enrolled, enrolling...");
            //TODO: get user
            User user = new User();
            startEnrollment(enrollId, user);
        } else {
            deviceDao.sendMessage("ID " + enrollId + " already enrolled.");
        }
    }
}
