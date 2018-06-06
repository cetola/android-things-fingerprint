package edu.pdx.ekbotecetolafinalpi.uart;

import java.util.HashMap;
import java.util.Map;

public class ErrorMap {
    public static final Character NO_ERROR                     = 0x0000;  // Default value. no error
    public static final Character NACK_TIMEOUT                 = 0x1001;  // Obsolete, capture timeout
    public static final Character NACK_INVALID_BAUDRATE        = 0x1002;  // Obsolete, Invalid serial baud rate
    public static final Character NACK_INVALID_POS             = 0x1003;  // The specified ID is not between 0~199
    public static final Character NACK_IS_NOT_USED             = 0x1004;  // The specified ID is not used
    public static final Character NACK_IS_ALREADY_USED         = 0x1005;  // The specified ID is already used
    public static final Character NACK_COMM_ERR                = 0x1006;  // Communication Error
    public static final Character NACK_VERIFY_FAILED           = 0x1007;  // 1:1 Verification Failure
    public static final Character NACK_IDENTIFY_FAILED         = 0x1008;  // 1:N Identification Failure
    public static final Character NACK_DB_IS_FULL              = 0x1009;  // The database is full
    public static final Character NACK_DB_IS_EMPTY             = 0x100A;  // The database is empty
    public static final Character NACK_TURN_ERR                = 0x100B;  // Obsolete, Invalid order of the enrollment (The order was not as= EnrollStart -> Enroll1 -> Enroll2 -> Enroll3)
    public static final Character NACK_BAD_FINGER              = 0x100C;  // Too bad fingerprint
    public static final Character NACK_ENROLL_FAILED           = 0x100D;  // Enrollment Failure
    public static final Character NACK_IS_NOT_SUPPORTED        = 0x100E;  // The specified command is not supported
    public static final Character NACK_DEV_ERR                 = 0x100F;  // Device Error, especially if Crypto-Chip is trouble
    public static final Character NACK_CAPTURE_CANCELED        = 0x1010;  // Obsolete, The capturing is canceled
    public static final Character NACK_INVALID_PARAM           = 0x1011;  // Invalid parameter
    public static final Character NACK_FINGER_IS_NOT_PRESSED   = 0x1012;  // Finger is not pressed
    public static final Character INVALID                      = 0XFFFF;  // Used when parsing fails

    public static final Map<Character, String> erorrList = createMap();
    private static Map<Character, String> createMap()
    {
        Map<Character,String> myMap = new HashMap<>();
        myMap.put(NO_ERROR, "No error");
        myMap.put(NACK_TIMEOUT, "Obsolete, capture timeout");
        myMap.put(NACK_INVALID_BAUDRATE, "Obsolete, Invalid serial baud rate");
        myMap.put(NACK_INVALID_POS, "The specified ID is not between 0~199");
        myMap.put(NACK_IS_NOT_USED, "The specified ID is not used");
        myMap.put(NACK_IS_ALREADY_USED, "The specified ID is already used");
        myMap.put(NACK_COMM_ERR, "Communication Error");
        myMap.put(NACK_VERIFY_FAILED, "1:1 Verification Failure");
        myMap.put(NACK_IDENTIFY_FAILED, "1:N Identification Failure");
        myMap.put(NACK_DB_IS_FULL, "The database is full");
        myMap.put(NACK_DB_IS_EMPTY, "The database is empty");
        myMap.put(NACK_TURN_ERR, "Obsolete, Invalid order of the enrollment");
        myMap.put(NACK_BAD_FINGER, "Bad fingerprint");
        myMap.put(NACK_ENROLL_FAILED, "Enrollment Failure");
        myMap.put(NACK_IS_NOT_SUPPORTED, "The specified command is not supported");
        myMap.put(NACK_DEV_ERR, "Device Error, especially if Crypto-Chip is trouble");
        myMap.put(NACK_CAPTURE_CANCELED, "Obsolete, capturing is canceled");
        myMap.put(NACK_INVALID_PARAM, "Invalid parameter");
        myMap.put(NACK_FINGER_IS_NOT_PRESSED, "Finger is not pressed");
        myMap.put(INVALID, "Used when parsing fails");
        return myMap;
    }
}
