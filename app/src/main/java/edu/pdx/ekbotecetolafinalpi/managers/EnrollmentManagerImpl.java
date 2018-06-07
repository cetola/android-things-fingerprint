package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import edu.pdx.ekbotecetolafinalpi.account.Enrollment;
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
    private int enrollId = 1;

    public EnrollmentManagerImpl(UartManager uartManager, DeviceDao dd) {
        enrollState = Enrollment.NOT_ENROLLING;
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

    private void doAck(Response rsp) {
        switch (enrollState) {
            case Enrollment.NOT_ENROLLING:
                //do nothing
                break;
            case Enrollment.ENROLL_COUNT:
                showCount(rsp);
                break;
            case Enrollment.ENROLL_FAIL:
                error();
                break;
            default:
                step(rsp);
                break;
        }
    }

    private void doNack(Response rsp) {
        Log.d(TAG, "doNack: error: " + rsp.getError());
        Log.d(TAG, "doNack: -----------------enrollState: " + enrollState);
        uartManager.queueCommand(new Command(0, CommandMap.CmosLed));
    }

    private void step(Response rsp) {
        switch (enrollState) {
            case Enrollment.LED_ON:
                enrollState = nextState;
                nextState = Enrollment.FINGER_PRESS;
                sendEnrollCommand();
                break;
            case Enrollment.FINGER_PRESS:
                if(rsp.getParams() > 0) {
                    getFingerPress();
                } else {
                    enrollState = nextState;
                    nextState = enrollNumber;
                    deviceDao.sendMessage("Capturing finger image.");
                    sendCommand(new Command(0, CommandMap.CaptureFinger));
                }
                break;
            case Enrollment.ENROLL_START:
                getNextTemplate();
                break;
            case Enrollment.ENROLL_1:
                deviceDao.sendMessage("Please press finger (2nd template)");
                getNextTemplate();
                break;
            case Enrollment.ENROLL_2:
                deviceDao.sendMessage("Please press finger (3rd template)");
                getNextTemplate();
                break;
            case Enrollment.ENROLL_3:
                Log.i(TAG, "step: DONE!");
                enrollState = Enrollment.NOT_ENROLLING;
                nextState = Enrollment.NOT_ENROLLING;
                uartManager.queueCommand(new Command(0, CommandMap.CmosLed));
                break;
            case Enrollment.CAPTURE_FINGER:
                enrollState = nextState;
                nextState = Enrollment.FINGER_PRESS;
                sendEnrollCommand();
                break;
        }
    }

    private void getNextTemplate() {
        enrollState = nextState;
        nextState = Enrollment.CAPTURE_FINGER;
        deviceDao.sendMessage("Check for finger press.");
        sendCommand(new Command(0, CommandMap.IsPressFinger));
    }

    private void sendEnrollCommand() {
        switch (enrollNumber) {
            case Enrollment.ENROLL_START:
                deviceDao.sendMessage("Enrollment start.");
                sendCommand(new Command(enrollId, CommandMap.EnrollStart));
                break;
            case Enrollment.ENROLL_1:
                deviceDao.sendMessage("Enroll 1.");
                sendCommand(new Command(enrollId, CommandMap.Enroll1));
                break;
            case Enrollment.ENROLL_2:
                deviceDao.sendMessage("Enroll 2");
                sendCommand(new Command(enrollId, CommandMap.Enroll2));
                break;
            case Enrollment.ENROLL_3:
                deviceDao.sendMessage("Enroll 3");
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
            return Enrollment.LED_OFF;
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
        enrollState = Enrollment.NOT_ENROLLING;
        uartManager.queueCommand(new Command(0, CommandMap.CmosLed));
    }

    private void sendCommand(Command cmd) {
        uartManager.queueCommand(cmd);
    }

    public void getEnrollmentCount() {
        uartManager.queueCommand(new Command(0, CommandMap.GetEnrollCount));
    }

    public void startEnrollment() {
        deviceDao.sendMessage("Starting enrollment, LED ON");
        enrollState = Enrollment.LED_ON;
        nextState = Enrollment.ENROLL_START;
        enrollNumber = Enrollment.ENROLL_START;
        uartManager.queueCommand(new Command(1, CommandMap.CmosLed));
    }

    private void showCount(Response rsp) {
        Log.i(TAG, "showCount: got count: " + rsp.getParams());
    }
}
