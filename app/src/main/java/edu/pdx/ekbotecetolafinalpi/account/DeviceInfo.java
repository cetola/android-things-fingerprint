package edu.pdx.ekbotecetolafinalpi.account;

/**
 * DeviceInfo object does not currently get save to the database.
 *
 * This can be used in the future to provide information about the fingerprint device to the user.
 */
public class DeviceInfo {
    private String firmwareVersion;
    private String isoMaxSize;
    private String deviceSN;

    public static final int VERSION_OFFSET = 8;
    public static final int ISO_SIZE_OFFSET = 16;
    public static final int DEVICE_SN_OFFSET = 24;

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        char[] chars = firmwareVersion.toCharArray();
        this.firmwareVersion = new StringBuilder().append(chars[6]).append(chars[7])
                .append(chars[4]).append(chars[5])
                .append(chars[2]).append(chars[3])
                .append(chars[0]).append(chars[1])
                .toString();
    }

    public String getIsoMaxSize() {
        return isoMaxSize;
    }

    public void setIsoMaxSize(String isoMaxSize) {
        this.isoMaxSize = isoMaxSize;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "firmwareVersion='" + getFirmwareVersion() + '\'' +
                ", isoMaxSize='" + getIsoMaxSize() + '\'' +
                ", deviceSN='" + getDeviceSN() + '\'' +
                '}';
    }
}
