package edu.pdx.ekbotecetolafinalpi.dao;

/**
 * Data Access Object for interacting with the Android Device.
 *
 * See the RegistrationFingerprintMsg, RegisterFingerprintStatus, and UnlockStatus objects for
 * possible values.
 */
public interface DeviceDao {
    /**
     * Sets the message the users sees while registering their fingerprint.
     * @param message
     */
    void setRegisterFingerprintMsg(String message);

    /**
     * Sets the registration status. For example, if failed, set this to "FAIL".
     * @param status
     */
    void setRegisterFingerprintStatus(String status);

    /**
     * Sets the status of the "lockbox"
     * @param status
     */
    void setUnlockStatus(String status);
}
