package edu.pdx.ekbotecetolafinalpi.uart;

import java.util.HashMap;
import java.util.Map;

public class CommandList {
    public static final Character NotSet                 = 0x00;        // Default value for enum. Scanner will return error if sent this.
    public static final Character Open                   = 0x01;        // Open Initialization
    public static final Character Close                  = 0x02;        // Close Termination
    public static final Character UsbInternalCheck       = 0x03;        // UsbInternalCheck Check if the connected USB device is valid
    public static final Character ChangeBaudrate         = 0x04;        // ChangeBaudrate Change UART baud rate
    public static final Character SetIAPMode             = 0x05;        // SetIAPMode Enter IAP Mode In this mode; FW Upgrade is available
    public static final Character CmosLed                = 0x12;        // CmosLed Control CMOS LED
    public static final Character GetEnrollCount         = 0x20;        // Get enrolled fingerprint count
    public static final Character CheckEnrolled          = 0x21;        // Check whether the specified ID is already enrolled
    public static final Character EnrollStart            = 0x22;        // Start an enrollment
    public static final Character Enroll1                = 0x23;        // Make 1st template for an enrollment
    public static final Character Enroll2                = 0x24;        // Make 2nd template for an enrollment
    public static final Character Enroll3                = 0x25;        // Make 3rd template for an enrollment; merge three templates into one template; save merged template to the database
    public static final Character IsPressFinger          = 0x26;        // Check if a finger is placed on the sensor
    public static final Character DeleteID               = 0x40;        // Delete the fingerprint with the specified ID
    public static final Character DeleteAll              = 0x41;        // Delete all fingerprints from the database
    public static final Character Verify1_1              = 0x50;        // Verification of the capture fingerprint image with the specified ID
    public static final Character Identify1_N            = 0x51;        // Identification of the capture fingerprint image with the database
    public static final Character VerifyTemplate1_1      = 0x52;        // Verification of a fingerprint template with the specified ID
    public static final Character IdentifyTemplate1_N    = 0x53;        // Identification of a fingerprint template with the database
    public static final Character CaptureFinger          = 0x60;        // Capture a fingerprint image(256x256) from the sensor
    public static final Character MakeTemplate           = 0x61;        // Make template for transmission
    public static final Character GetImage               = 0x62;        // Download the captured fingerprint image(256x256)
    public static final Character GetRawImage            = 0x63;        // Capture & Download raw fingerprint image(320x240)
    public static final Character GetTemplate            = 0x70;        // Download the template of the specified ID
    public static final Character SetTemplate            = 0x71;        // Upload the template of the specified ID
    public static final Character GetDatabaseStart       = 0x72;        // Start database download; obsolete
    public static final Character GetDatabaseEnd         = 0x73;        // End database download; obsolete
    public static final Character UpgradeFirmware        = 0x80;        // Not supported
    public static final Character UpgradeISOCDImage      = 0x81;        // Not supported
    public static final Character Ack                    = 0x30;        // Acknowledge.
    public static final Character Nack                   = 0x31;        // Non-acknowledge

    public static final Map<Character, String> commandList = createMap();
    private static Map<Character, String> createMap()
    {
        Map<Character,String> myMap = new HashMap<>();
        myMap.put(NotSet, "NotSet");
        myMap.put(Open, "Open");
        myMap.put(Close, "Close");
        myMap.put(UsbInternalCheck, "UsbInternalCheck");
        myMap.put(ChangeBaudrate, "ChangeBaudrate");
        myMap.put(SetIAPMode, "SetIAPMode");
        myMap.put(CmosLed, "CmosLed");
        myMap.put(GetEnrollCount, "GetEnrollCount");
        myMap.put(CheckEnrolled, "CheckEnrolled");
        myMap.put(EnrollStart, "EnrollStart");
        myMap.put(Enroll1, "Enroll1");
        myMap.put(Enroll2, "Enroll2");
        myMap.put(Enroll3, "Enroll3");
        myMap.put(IsPressFinger, "IsPressFinger");
        myMap.put(DeleteID, "DeleteID");
        myMap.put(DeleteAll, "DeleteAll");
        myMap.put(Verify1_1, "Verify1_1");
        myMap.put(Identify1_N, "Identify1_N");
        myMap.put(VerifyTemplate1_1, "VerifyTemplate1_1");
        myMap.put(IdentifyTemplate1_N, "IdentifyTemplate1_N");
        myMap.put(CaptureFinger, "CaptureFinger");
        myMap.put(MakeTemplate, "MakeTemplate");
        myMap.put(GetImage, "GetImage");
        myMap.put(GetRawImage, "GetRawImage");
        myMap.put(GetTemplate, "GetTemplate");
        myMap.put(SetTemplate, "SetTemplate");
        myMap.put(GetDatabaseStart, "GetDatabaseStart");
        myMap.put(GetDatabaseEnd, "GetDatabaseEnd");
        myMap.put(UpgradeFirmware, "UpgradeFirmware");
        myMap.put(UpgradeISOCDImage, "UpgradeISOCDImage");
        myMap.put(Ack, "Ack");
        myMap.put(Nack, "Nack");
        return myMap;
    }

}
