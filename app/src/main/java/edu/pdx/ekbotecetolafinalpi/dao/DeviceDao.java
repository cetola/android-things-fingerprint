package edu.pdx.ekbotecetolafinalpi.dao;

public interface DeviceDao {
    void sendMessage(String message);
    void setUnlockStatus(String status);
}
