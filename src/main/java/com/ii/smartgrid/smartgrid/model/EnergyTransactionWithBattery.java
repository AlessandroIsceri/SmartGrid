package com.ii.smartgrid.smartgrid.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ii.smartgrid.smartgrid.model.CustomObject.Priority; 

public class EnergyTransactionWithBattery extends EnergyTransaction{

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

    public double sendBatteryEnergy(double energySent){
       return battery.requestEnergy(energySent);
    }

    public double receiveBatteryEnergy(double energyReceived) {
        double excess = battery.fillBattery(energyReceived);
        curTurnReceivedEnergy = curTurnReceivedEnergy + energyReceived - excess;
        return excess;
    }

    public double getMissingEnergyForThreshold(double threhsoldPercentage){
        //0.25 -> 0.75  
        //1000*0.75 - 250 = 750-250 = 500

        //maxBattery = 10000
        //threshold = 0.75
        //curBattery = 5000
        //maxEnergyInTurn 1000

        //energyRequired = 2500
        
        double energyRequired = battery.getMaxCapacityInWatt() * threhsoldPercentage - battery.getStoredEnergy();
        double maxEnergyInTurn = battery.getMaxEnergyInTurn() - curTurnReceivedEnergy;

        return Math.min(energyRequired, maxEnergyInTurn);
    }

    @Override
    public String toString() {
        return "EnergyTransactionWithBattery [battery=" + battery + ", priority=" + priority
                + ", energyTransactionValue=" + energyTransactionValue + ", nodeName=" + nodeName
                + ", batteryAvailable=" + batteryAvailable + ", transactionType=" + transactionType + "]";
    }

    @JsonIgnore
    public boolean isCharged(double threshold) {
        return battery.getStateOfCharge() > threshold;
    }

    public boolean hasReachedLimit() {
        return curTurnReceivedEnergy >= battery.getMaxEnergyInTurn() - 0.01; 
    }

    

    

    
    
}
