package com.ii.smartgrid.model.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.ii.smartgrid.model.entities.CustomObject.Priority;

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
            energyTransactionValue = Math.max(0.0, energyTransactionValue);
        }
    }

    public void sendEnergy(double energy) {
        if (transactionType == TransactionType.SEND) {
            this.energyTransactionValue -= energy;
            energyTransactionValue = Math.max(0.0, energyTransactionValue);
        }
    }

    @Override
    public String toString() {
        return "EnergyTransaction [priority=" + priority + ", energyTransactionValue=" + energyTransactionValue
                + ", nodeName=" + nodeName + ", batteryAvailable=" + batteryAvailable + ", transactionType="
                + transactionType + "]";
    }

    public enum TransactionType {RECEIVE, SEND}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        long temp;
        temp = Double.doubleToLongBits(energyTransactionValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
        result = prime * result + (batteryAvailable ? 1231 : 1237);
        result = prime * result + ((transactionType == null) ? 0 : transactionType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EnergyTransaction other = (EnergyTransaction) obj;
        if (priority != other.priority)
            return false;
        if (Double.doubleToLongBits(energyTransactionValue) != Double.doubleToLongBits(other.energyTransactionValue))
            return false;
        if (nodeName == null) {
            if (other.nodeName != null)
                return false;
        } else if (!nodeName.equals(other.nodeName))
            return false;
        if (batteryAvailable != other.batteryAvailable)
            return false;
        if (transactionType != other.transactionType)
            return false;
        return true;
    }

    

}