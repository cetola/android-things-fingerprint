package edu.pdx.ekbotecetolafinalpi.uart;

public class UartUtils {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes, Integer length) {
        int l = (length == null)? bytes.length : length;
        char[] hexChars = new char[l * 2];
        for ( int j = 0; j < l; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
