package com.ii.smartgrid.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ii.smartgrid.utils.TimeUtils;

public class Battery {

    private double voltage;
    private double maxCapacityInWattHour;
    private double maxCapacityInAmpHour;
    private double storedEnergy;
    private double dischargeCurrent;
    private double efficiency;
    // State of Charge between 0 and 1 indicating the battery percentage
    private double stateOfCharge;

    public Battery() {
        super();
    }

    public double fillBattery(double energyWH) {
        double maxEnergyInTurn = getMaxEnergyInTurn(); // WH
        double effectiveFillEnergy = Math.min(maxEnergyInTurn, energyWH);
        double residual = Math.max(0.0, energyWH - maxEnergyInTurn);

        // If the energy is more than the amount that can be stored, store 
        // the possible amount and return the exceeding energy
        if (storedEnergy + effectiveFillEnergy <= maxCapacityInWattHour) {
            storedEnergy += effectiveFillEnergy;
            stateOfCharge = storedEnergy / maxCapacityInWattHour;
            return residual;
        } else {
            double excess = storedEnergy + effectiveFillEnergy - maxCapacityInWattHour;
            storedEnergy = maxCapacityInWattHour;
            stateOfCharge = 1.0;
            return residual + excess;
        }
    }

    @JsonIgnore
    public double getAvailableEnergy() {
        return Math.min(getMaxEnergyInTurn(), storedEnergy);
    }

    public double getDischargeCurrent() {
        return dischargeCurrent;
    }

    public void setDischargeCurrent(double dischargeCurrent) {
        this.dischargeCurrent = dischargeCurrent / 2;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public double getMaxCapacityInAmpHour() {
        return maxCapacityInAmpHour;
    }

    public void setMaxCapacityInAmpHour(double maxCapacityInAmpHour) {
        this.maxCapacityInAmpHour = maxCapacityInAmpHour;
        this.maxCapacityInWattHour = maxCapacityInAmpHour * voltage;
    }

    public double getMaxCapacityInWattHour() {
        return maxCapacityInWattHour;
    }

    public void setMaxCapacityInWattHour(double maxCapacityInWattHour) {
        this.maxCapacityInWattHour = maxCapacityInWattHour;
        this.maxCapacityInAmpHour = maxCapacityInWattHour / voltage;
    }

    @JsonIgnore
    public double getMaxEnergyInTurn() {
        return voltage * dischargeCurrent * efficiency * TimeUtils.getTurnDurationHours(); // WH
    }

    public double getStateOfCharge() {
        return stateOfCharge;
    }

    public void setStateOfCharge(double stateOfCharge) {
        this.stateOfCharge = stateOfCharge;
    }

    public double getStoredEnergy() {
        return storedEnergy;
    }

    public void setStoredEnergy(double storedEnergy) {
        this.storedEnergy = storedEnergy;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    // Same as fillBattery but with energy discharge instead of charge
    public double requestEnergy(double requestedEnergyWH) {
        double maxEnergyInTurn = getMaxEnergyInTurn(); // WH
        double effectiveRequestedEnergy = Math.min(maxEnergyInTurn, requestedEnergyWH);

        double remainingEnergy = storedEnergy - effectiveRequestedEnergy;

        if (remainingEnergy >= 0.01) {
            storedEnergy -= effectiveRequestedEnergy;
            stateOfCharge = storedEnergy / maxCapacityInWattHour;
            return effectiveRequestedEnergy;
        } else {
            double oldStored = storedEnergy;
            storedEnergy = 0.0;
            stateOfCharge = 0.0;
            return oldStored;
        }
    }

    @Override
    public String toString() {
        return "Battery [voltage=" + voltage + ", maxCapacityInWattHour=" + maxCapacityInWattHour + ", maxCapacityInAmpHour="
                + maxCapacityInAmpHour + ", storedEnergy=" + storedEnergy + ", dischargeCurrent=" + dischargeCurrent
                + ", efficiency=" + efficiency + ", stateOfCharge=" + stateOfCharge + "]";
    }

}
