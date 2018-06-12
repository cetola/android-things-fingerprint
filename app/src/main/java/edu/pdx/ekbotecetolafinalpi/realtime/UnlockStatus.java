package edu.pdx.ekbotecetolafinalpi.realtime;

/**
 * A static list of messages that the Realtime database should get.
 */
public class UnlockStatus {
    public static final String COLLECTION = UnlockStatus.class.getSimpleName();
    //we may need this at some point
    @SuppressWarnings("unused")
    public static final String NONE = "None";
    public static final String REQUEST = "Request";
    public static final String UNLOCKED = "Unlocked";
    public static final String PROCESS = "Processing";
    public static final String FAIL = "Failed";
}
