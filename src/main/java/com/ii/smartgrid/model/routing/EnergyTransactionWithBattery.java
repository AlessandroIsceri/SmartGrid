package com.ii.smartgrid.model.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ii.smartgrid.model.Battery;
import com.ii.smartgrid.model.entities.CustomObject.Priority;

public class EnergyTransactionWithBattery extends EnergyTransaction {

    public static final double FULL_BATTERY = 1.0;

    private double curTurnReceivedEnergy;

    private Battery battery;

    public EnergyTransactionWithBattery() {
        super();
        this.batteryAvailable = true;
    }

    public EnergyTransactionWithBattery(Priority priority, double energyTransactionValue, String nodeName, Battery battery, TransactionType transactionType) {
        super(priority, energyTransactionValue, nodeName, transactionType);
        this.batteryAvailable = true;
        this.battery = battery;
        curTurnReceivedEnergy = 0;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    // Compute and returns the amount of energy needed to reach the given threshold of charge
    public double getMissingEnergyForThreshold(double threhsoldPercentage) {
        double energyRequired = battery.getMaxCapacityInWatt() * threhsoldPercentage - battery.getStoredEnergy();
        double maxEnergyInTurn = battery.getMaxEnergyInTurn() - curTurnReceivedEnergy;

        return Math.min(energyRequired, maxEnergyInTurn);
    }

    @JsonIgnore
    public double getStateOfCharge() {
        return battery.getStateOfCharge();
	}

    public boolean hasReachedLimit() {
        return curTurnReceivedEnergy >= battery.getMaxEnergyInTurn() - 0.01;
    }

    @JsonIgnore
    public boolean isCharged(double threshold) {
        return battery.getStateOfCharge() > threshold;
    }

    public double receiveBatteryEnergy(double energyReceived) {
        double excess = battery.fillBattery(energyReceived);
        curTurnReceivedEnergy = curTurnReceivedEnergy + energyReceived - excess;
        return excess;
    }

    public double sendBatteryEnergy(double energySent) {
        return battery.requestEnergy(energySent);
    }

    @Override
    public String toString() {
        return "EnergyTransactionWithBattery [battery=" + battery + ", priority=" + priority
                + ", energyTransactionValue=" + energyTransactionValue + ", nodeName=" + nodeName
                + ", batteryAvailable=" + batteryAvailable + ", transactionType=" + transactionType + "]";
    }

}
