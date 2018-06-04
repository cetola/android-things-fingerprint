package edu.pdx.ekbotecetolafinalpi.dao;

import edu.pdx.ekbotecetolafinalpi.uart.Command;
import edu.pdx.ekbotecetolafinalpi.uart.DataPacket;
import edu.pdx.ekbotecetolafinalpi.uart.Response;

public interface MessageDao {
    void saveCommand(Command cmd);
    Command getCommand(String id);
    void setCommandComplete(Command cmd);
    void saveResponse(Response rsp);
    void saveDataPacket(DataPacket dp);
}
