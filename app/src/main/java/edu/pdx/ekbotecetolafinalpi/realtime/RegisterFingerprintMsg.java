package edu.pdx.ekbotecetolafinalpi.realtime;

/**
 * A static list of messages that the Realtime database should get.
 */
public class RegisterFingerprintMsg {
    public static final String COLLECTION = RegisterFingerprintMsg.class.getSimpleName();
    public static final String NONE = "None";
    public static final String PLACE = "Place finger on scanner";
    public static final String LIFT = "Lift finger from scanner";
    public static final String TRY = "Try again";
}
