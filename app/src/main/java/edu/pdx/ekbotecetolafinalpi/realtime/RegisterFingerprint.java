package edu.pdx.ekbotecetolafinalpi.realtime;

/**
 * A static list of messages that the Realtime database should get.
 */
public class RegisterFingerprint {
    public static final String COLLECTION = RegisterFingerprint.class.getSimpleName();
    //we may need this at some point
    @SuppressWarnings("unused")
    public static final String NONE = "None";
    public static final String START = "Start";
    public static final String FINISHED = "Finished";
    public static final String FAILED = "Failed";
}
