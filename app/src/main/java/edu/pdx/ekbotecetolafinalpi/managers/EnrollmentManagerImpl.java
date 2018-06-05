package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import edu.pdx.ekbotecetolafinalpi.account.Enrollment;
import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.CommandList;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

public class EnrollmentManagerImpl implements EnrollmentManager {
    private static final String TAG = "EnrollmentManagerImpl";
    private UartManager uartManager;
    private int enrollState;
    private int nextState;
    private int attempts;
    private int enrollNumber;

    public EnrollmentManagerImpl(UartManager uartManager) {
        enrollState = Enrollment.NOT_ENROLLING;
        attempts = 0;
        this.uartManager = uartManager;
        uartManager.setResponseListener(new UartManager.ResponseReadyListener() {
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
            case Enrollment.ENROLL_3:
                finish();
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
        switch (enrollState) {
            case Enrollment.NOT_ENROLLING:
                //do nothing
                break;
            case Enrollment.FINGER_PRESS:
                Log.i(TAG, "doNack: finger press");
            default:
                enrollFail();
                break;
        }
    }

    private void step(Response rsp) {
        Log.d(TAG, "step: state: " + enrollState);
        switch (enrollState) {
            case Enrollment.LED_ON:
                getFingerPress();
                enrollState = nextState;
                nextState = enrollNumber;
                break;
            case Enrollment.FINGER_PRESS:
                if(rsp.getParams() > 0) {
                    getFingerPress();
                } else {
                    enrollState = nextState;
                    nextState = nextEnroll();
                    sendCommand(new Command(0, CommandList.EnrollStart));
                }
                break;
            case Enrollment.ENROLL_START:
                enrollState = nextState;
                nextState = nextEnroll();
                sendCommand(new Command(0, CommandList.Enroll1));
                break;
            case Enrollment.ENROLL_1:
                enrollState = nextState;
                nextState = nextEnroll();
                sendCommand(new Command(0, CommandList.Enroll2));
                break;
            case Enrollment.ENROLL_2:
                enrollState = nextState;
                nextState = nextEnroll();
                sendCommand(new Command(0, CommandList.Enroll3));
                break;
            case Enrollment.ENROLL_3:
                Log.i(TAG, "step: DONE!");
                enrollState = Enrollment.NOT_ENROLLING;
                nextState = Enrollment.NOT_ENROLLING;
                uartManager.queueCommand(new Command(0, CommandList.CmosLed));
                break;
        }
    }

    private int nextEnroll() {
        if(enrollNumber < 4) {
            enrollNumber++;
            return enrollNumber;
        } else {
            return Enrollment.LED_OFF;
        }
    }

    private void getFingerPress() {
        attempts++;
        if(attempts < 4) {
            sendCommand(new Command(0, CommandList.IsPressFinger));
        } else {
            enrollFail();
            attempts = 0;
        }
    }

    private void finish() {
        Log.d(TAG, "finish: state: " + enrollState);
    }

    private void error() {
        Log.e(TAG, "error: got ack in enroll state: " + enrollState);
    }

    private void enrollFail() {
        Log.e(TAG, "enrollFail: state: " + enrollState);
        enrollState = Enrollment.NOT_ENROLLING;
        uartManager.queueCommand(new Command(0, CommandList.CmosLed));
    }

    private void sendCommand(Command cmd) {
        uartManager.queueCommand(cmd);
    }

    public void getEnrollmentCount() {
        uartManager.queueCommand(new Command(0, CommandList.GetEnrollCount));
    }

    public void startEnrollment() {
        enrollState = Enrollment.LED_ON;
        nextState = Enrollment.FINGER_PRESS;
        enrollNumber = Enrollment.ENROLL_START;
        uartManager.queueCommand(new Command(1, CommandList.CmosLed));
    }

    private void showCount(Response rsp) {
        Log.i(TAG, "showCount: got count: " + rsp.getParams());
    }
}
