package edu.pdx.ekbotecetolafinalpi.managers;

import edu.pdx.ekbotecetolafinalpi.uart.Response;

public abstract class FiniteStateMachineManager {

    protected UartManager uartManager;
    protected FirestoreManager dbManager;
    protected int state;
    protected int nextState;

    public FiniteStateMachineManager(UartManager uartManager, FirestoreManager dbManager) {
        this.uartManager = uartManager;
        this.dbManager = dbManager;
        this.uartManager = uartManager;
        this.uartManager.setResponseListener(new UartManager.ResponseReadyListener() {
            @Override
            public void onResponseReady(Response rsp) {
                if(!rsp.isEmpty()) {
                    if(!rsp.getAck()) {
                        doNack(rsp);
                    } else {
                        doAck(rsp);
                    }
                }
            }
        });
    }

    abstract void doAck(Response rsp);
    abstract void doNack(Response rsp);
}
