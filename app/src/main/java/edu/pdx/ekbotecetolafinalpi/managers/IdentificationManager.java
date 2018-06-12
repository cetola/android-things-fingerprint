package edu.pdx.ekbotecetolafinalpi.managers;

/**
 * A manager for all "identification" tasks.
 */
public interface IdentificationManager {
    /**
     * Identifies a given fingerprint for a user. Unlocks the "box".
     * @param userId
     */
    void identifyFinger(String userId);
}
