package edu.pdx.ekbotecetolafinalpi.dao;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.DataPacket;
import edu.pdx.ekbotecetolafinalpi.uart.Response;


/**
 * Data Access Object for Responses, DataPackets, and Commands
 */
public interface MessageDao {

    /**
     * Saves a command that has been sent. Handy for debugging live instances.
     * @param cmd
     * @param result
     */
    void saveCommand(Command cmd, OnSuccessListener<DocumentReference> result);

    /**
     * Saves a response that has been received. Handy for debugging live instances.
     * @param rsp
     * @param result
     */
    void saveResponse(Response rsp, OnSuccessListener<DocumentReference> result);

    /**
     * A response should contain the command reference from the database. This is a convenience
     * method to return that reference.
     * @param id
     * @return
     */
    DocumentReference getCommandRef(String id);
}
