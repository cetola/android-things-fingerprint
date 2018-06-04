package edu.pdx.ekbotecetolafinalpi.uart;

import java.util.ArrayDeque;
import java.util.Queue;

import edu.pdx.ekbotecetolafinalpi.dao.MessageDao;
import edu.pdx.ekbotecetolafinalpi.dao.MessageDaoImpl;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManager;
import edu.pdx.ekbotecetolafinalpi.managers.FirestoreManagerImpl;

public class CommandQueue {
    private Queue<Command> commands;
    MessageDao msgDao;
    FirestoreManager dbManager;

    public CommandQueue() {
        dbManager = new FirestoreManagerImpl();
        msgDao = new MessageDaoImpl(dbManager);
        commands = new ArrayDeque<>();
    }

    public void addCommand(Command cmd) {
        msgDao.saveCommand(cmd);
        commands.add(cmd);
    }

    public Command getNextCommand() {
        return commands.remove();
    }

    public void completeCommand(Command cmd, Response rsp, DataPacket dp) {
        msgDao.setCommandComplete(cmd);
        msgDao.saveResponse(rsp);
        if(dp != null) {
            msgDao.saveDataPacket(dp);
        }
    }

    public int getSize() {
        return commands.size();
    }
}
