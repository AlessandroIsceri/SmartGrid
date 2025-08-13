package com.ii.smartgrid.smartgrid.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
