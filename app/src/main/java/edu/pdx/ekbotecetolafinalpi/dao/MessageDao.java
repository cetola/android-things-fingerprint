package edu.pdx.ekbotecetolafinalpi.dao;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.DataPacket;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

public interface MessageDao {
    void saveCommand(Command cmd, OnSuccessListener<DocumentReference> result);
    Command getCommand(String id);
    void saveResponse(Response rsp, OnSuccessListener<DocumentReference> result);
    void saveDataPacket(DataPacket dp);
    DocumentReference getCommandRef(String id);
}
