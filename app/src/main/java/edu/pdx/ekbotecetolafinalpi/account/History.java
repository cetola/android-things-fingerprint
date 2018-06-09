package edu.pdx.ekbotecetolafinalpi.account;

import com.google.firebase.Timestamp;

import java.util.Date;

public class History {

    public static final String COLLECTION = "history";
    String userName;
    Timestamp unlockStamp;

    public History() {
    }

    public History(String usernName) {
        setUserName(usernName);
        setUnlockStamp(new Timestamp(new Date()));
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getUnlockStamp() {
        return unlockStamp;
    }

    public void setUnlockStamp(Timestamp unlockStamp) {
        this.unlockStamp = unlockStamp;
    }
}
