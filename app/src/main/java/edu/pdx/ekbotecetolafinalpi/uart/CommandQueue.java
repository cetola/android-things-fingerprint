package edu.pdx.ekbotecetolafinalpi.uart;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import edu.pdx.ekbotecetolafinalpi.dao.MessageDao;
import edu.pdx.ekbotecetolafinalpi.dao.MessageDaoImpl;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManager;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManagerImpl;

public class CommandQueue {
    private static final String TAG = "CommandQueue";
    private Queue<Command> commands;
    private Command currentCommand;
    private Map<Command, Response> handshake;
    private Map<Response, DataPacket> dataPacket;
    MessageDao msgDao;
    FirestoreManager dbManager;

    public CommandQueue(FirestoreManager dbManager) {
        this.dbManager = dbManager;
        msgDao = new MessageDaoImpl(dbManager);
        commands = new ArrayDeque<>();
        handshake = new HashMap<>();
        dataPacket = new HashMap<>();
    }

    public void addCommand(Command cmd) {
        msgDao.saveCommand(cmd, new OnSuccessListener<DocumentReference>() {
            private Command command;

            @Override
            public void onSuccess(DocumentReference documentReference) {
                command.setId(documentReference.getId());
                commands.add(command);
            }

            private OnSuccessListener<DocumentReference> init(Command cmd) {
                this.command = cmd;
                return this;
            }
        }.init(cmd) );
    }

    public void addResponse(Response response) {
        response.setCommand(currentCommand, msgDao.getCommandRef(currentCommand.getId()));
        msgDao.saveResponse(response, new OnSuccessListener<DocumentReference>() {
            private Response response;
            private Command command;
            @Override
            public void onSuccess(DocumentReference documentReference) {
                response.setId(documentReference.getId());
                handshake.put(this.command, this.response);
            }

            private OnSuccessListener<DocumentReference> init(Response rsp, Command cmd) {
                this.response = rsp;
                this.command = cmd;
                return this;
            }
        }.init(response, currentCommand) );
    }

    public Command getNextCommand() {
        currentCommand = commands.remove();
        return currentCommand;
    }

    public int getSize() {
        return commands.size();
    }
}
