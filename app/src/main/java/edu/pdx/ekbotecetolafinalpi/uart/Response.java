package edu.pdx.ekbotecetolafinalpi.uart;

import com.google.firebase.firestore.DocumentReference;

/**
 * This object can be persisted in the Firestore for easy debugging.
 *
 * It represents a response from the fingerprint scanner.
 *
 * It also associates a given command with the response, since the fingerprint scanner will not
 * send messages without first receiving a command.
 */
public class Response extends Message {
    private static final String TAG = "Response";
    public static final String COLLECTION = "responses";
    private static final char ACK = 0x30;
    private static final int ACK_INDEX = 8;
    private static final int ERR_INDEX = 4;
    private DocumentReference cmdRef;
    private Command command;

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
        error = ErrorMap.erorrList.get(getData().getChar(ERR_INDEX));
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

    public void setCommand(Command command, DocumentReference cmdRef) {
        this.command = command;
        this.cmdRef = cmdRef;
    }

    /**
     * This is simply for debugging purposes.
     *
     * This will store a String "command name" in the Firestore. While this is redundant data, it
     * makes debugging much easier as a quick glance at the Firestore tells you what type of command
     * the response belongs to.
     * @return
     */
    @SuppressWarnings("unused")
    public String getCommandName() {
        return this.command.getName();
    }

    @SuppressWarnings("unused")
    public DocumentReference getCmdRef() {
        return cmdRef;
    }
}
