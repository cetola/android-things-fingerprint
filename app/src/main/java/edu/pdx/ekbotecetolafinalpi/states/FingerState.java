package edu.pdx.ekbotecetolafinalpi.states;

/**
 * States common to all Fingerprint reader state machines.
 */
public abstract class FingerState {
    public static final int NOT_ACTIVE = -1;
    public static final int FINGER_PRESS = -2;
    public static final int LED_ON = -3;
    public static final int LED_OFF = -4;
    public static final int CAPTURE_FINGER = -5;
}
