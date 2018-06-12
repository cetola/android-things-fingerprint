package edu.pdx.ekbotecetolafinalpi.managers;


/**
 * Manage the enrollment of users (add a fingerprint to the scanner).
 */
public interface EnrollmentManager {

    /**
     * Check to see if a given scanner id, user, and finger are registered. If they are not then
     * the enrollment process will start.
     * @param scannerId
     * @param finger
     * @param userId
     */
    void checkEnroll(int scannerId, int finger, String userId);

    /**
     * Delete all the data on the Fingerprint Scanner. For debugging and testing.
     */
    void deleteAll();
}
