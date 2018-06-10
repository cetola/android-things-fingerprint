package edu.pdx.ekbotecetolafinalpi.dao;

public interface DeviceDao {
    void setRegisterFingerprintMsg(String message);
    void setRegisterFingerprintStatus(String status);
    void setUnlockStatus(String status);
}
