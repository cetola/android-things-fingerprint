package edu.pdx.ekbotecetolafinalpi.managers;

import edu.pdx.ekbotecetolafinalpi.exceptions.ConnectionFailedException;

public interface FirestoreManager {
    int init() throws ConnectionFailedException;
}
