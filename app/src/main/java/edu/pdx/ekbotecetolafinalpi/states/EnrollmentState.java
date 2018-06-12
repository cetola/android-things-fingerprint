package edu.pdx.ekbotecetolafinalpi.states;

/**
 * States specific to the Enrollment state machine
 */
public class EnrollmentState extends FingerState {
    public static final int ENROLL_START = 0;
    public static final int ENROLL_1 = 1;
    public static final int ENROLL_2 = 2;
    public static final int ENROLL_3 = 3;
    public static final int ENROLL_FAIL = 4;
    public static final int CHECK_ENROLLED = 5;
    public static final int DELETE_ALL = 666;
}
