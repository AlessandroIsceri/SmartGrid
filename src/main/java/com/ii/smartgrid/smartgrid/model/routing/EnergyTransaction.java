package com.ii.smartgrid.smartgrid.model.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.ii.smartgrid.smartgrid.model.entities.CustomObject.Priority;

@JsonTypeInfo(
        use = Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")

@JsonSubTypes({
        @Type(value = EnergyTransactionWithBattery.class),
        @Type(value = EnergyTransactionWithoutBattery.class)
})

public abstract class EnergyTransaction {
    protected Priority priority;
    protected double energyTransactionValue;
    protected String nodeName;
    @JsonIgnore
    protected boolean batteryAvailable;
    protected TransactionType transactionType;
    protected EnergyTransaction() {
        super();
    }

    protected EnergyTransaction(Priority priority, double energyTransactionValue, String nodeName, TransactionType transactionType) {
        super();
        this.priority = priority;
        this.energyTransactionValue = energyTransactionValue;
        this.nodeName = nodeName;
        this.transactionType = transactionType;
    }

    public double getEnergyTransactionValue() {
        return energyTransactionValue;
    }

    public void setEnergyTransactionValue(double energyTransactionValue) {
        this.energyTransactionValue = energyTransactionValue;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public boolean isBatteryAvailable() {
        return batteryAvailable;
    }

    public void receiveEnergy(double energy) {
        if (transactionType == TransactionType.RECEIVE) {
            this.energyTransactionValue -= energy;
        }
    }

    public void sendEnergy(double energy) {
        if (transactionType == TransactionType.SEND) {
            this.energyTransactionValue -= energy;
        }
    }

    @Override
    public String toString() {
        return "EnergyTransaction [priority=" + priority + ", energyTransactionValue=" + energyTransactionValue
                + ", nodeName=" + nodeName + ", batteryAvailable=" + batteryAvailable + ", transactionType="
                + transactionType + "]";
    }

    public enum TransactionType {RECEIVE, SEND}

}