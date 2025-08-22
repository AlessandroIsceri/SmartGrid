package com.ii.smartgrid.model.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ii.smartgrid.model.Battery;
import com.ii.smartgrid.model.entities.CustomObject.Priority;

public class EnergyTransactionWithBattery extends EnergyTransaction {

    public static final double FULL_BATTERY = 1.0;

    private double curTurnReceivedEnergy;
    private double curTurnReleasedEnergy;

    private Battery battery;

    public EnergyTransactionWithBattery() {
        super();
        this.batteryAvailable = true;
        curTurnReceivedEnergy = 0;
        curTurnReleasedEnergy = 0;
    }

    public EnergyTransactionWithBattery(Priority priority, double energyTransactionValue, String nodeName, Battery battery, TransactionType transactionType) {
        super(priority, energyTransactionValue, nodeName, transactionType);
        this.batteryAvailable = true;
        this.battery = battery;
        curTurnReceivedEnergy = 0;
        curTurnReleasedEnergy = 0;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    // Compute and returns the amount of energy needed to reach the given threshold of charge
    public double getMissingEnergyForThreshold(double thresholdPercentage) {
        double energyRequired = battery.getMaxCapacityInWattHour() * thresholdPercentage - battery.getStoredEnergy();
        double maxEnergyInTurn = battery.getMaxEnergyInTurn() - curTurnReceivedEnergy - curTurnReleasedEnergy;
        return Math.max(0, Math.min(energyRequired, maxEnergyInTurn));
    }

    public boolean hasReachedLimit() {
        return (curTurnReceivedEnergy + curTurnReleasedEnergy) >= battery.getMaxEnergyInTurn() - 0.01;
    }

    private double willReachLimit(double energy){
        if(hasReachedLimit()){
            return 0;
        }
        if ((curTurnReceivedEnergy + curTurnReleasedEnergy + energy) >= battery.getMaxEnergyInTurn() - 0.01){
            return battery.getMaxEnergyInTurn() - curTurnReceivedEnergy - curTurnReleasedEnergy;
        } else {
            return energy;
        }
    }

    @JsonIgnore
    public boolean isCharged(double threshold) {
        return battery.getStateOfCharge() > threshold;
    }

    public double receiveBatteryEnergy(double energyReceived) {
        double possibleReceivableEnergy = willReachLimit(energyReceived);
        if(possibleReceivableEnergy == 0){
            return energyReceived;
        }
        double notReceivableEnergy = energyReceived - possibleReceivableEnergy;
        double excess = battery.fillBattery(possibleReceivableEnergy);
        curTurnReceivedEnergy = curTurnReceivedEnergy + possibleReceivableEnergy - excess;
        return excess + notReceivableEnergy;
    }

    public double sendBatteryEnergy(double energySent) {
        double possibleSendableEnergy = willReachLimit(energySent);
        if(possibleSendableEnergy == 0){
            return 0;
        }
        double effectiveSentEnergy = battery.requestEnergy(possibleSendableEnergy);
        curTurnReleasedEnergy += effectiveSentEnergy;
        return effectiveSentEnergy;
    }

    @Override
    public String toString() {
        return "EnergyTransactionWithBattery [battery=" + battery + ", priority=" + priority
                + ", energyTransactionValue=" + energyTransactionValue + ", nodeName=" + nodeName
                + ", batteryAvailable=" + batteryAvailable + ", transactionType=" + transactionType + "]";
    }

}
