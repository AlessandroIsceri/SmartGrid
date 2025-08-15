package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.model.CustomObject.Priority;

public class EnergyTransactionWithoutBattery extends EnergyTransaction{
     
    public EnergyTransactionWithoutBattery() {
        super();
        this.batteryAvailable = false;
    }

    public EnergyTransactionWithoutBattery(Priority priority, double energyTransactionValue, String nodeName, TransactionType transactionType) {
        super(priority, energyTransactionValue, nodeName, transactionType);
    }
}
