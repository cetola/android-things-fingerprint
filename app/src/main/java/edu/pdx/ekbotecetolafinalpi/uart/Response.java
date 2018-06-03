package edu.pdx.ekbotecetolafinalpi.uart;

public class Response extends Message {
    private static final String TAG = "Response";
    private static final char ACK = 0x30;
    private static final int ACK_INDEX = 8;
    private static final int ERR_INDEX = 4;

    private String error = "";
    private boolean ack = false;

    public Response() {
        super();
    }

    public boolean getAck() {
        if(!isEmpty()) {
            char check = getData().getChar(ACK_INDEX);
            ack = (check == ACK);
        }
        return ack;
    }

    public String getError() {
        error = Errors.erorrList.get(getData().getChar(ERR_INDEX));
        return error;
    }

    public boolean isEmpty() {
        for (byte b : getData().array()) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }
}
