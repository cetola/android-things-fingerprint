package edu.pdx.ekbotecetolafinalpi.managers;

import edu.pdx.ekbotecetolafinalpi.uart.Response;

public class IdentificationManagerImpl extends FiniteStateMachineManager implements IdentificationManager {

    public IdentificationManagerImpl(UartManager uartManager, FirestoreManager dbManager) {
        super(uartManager, dbManager);
    }

    @Override
    void doAck(Response rsp) {

    }

    @Override
    void doNack(Response rsp) {

    }

    @Override
    public void identifyFinger() {

    }
}
