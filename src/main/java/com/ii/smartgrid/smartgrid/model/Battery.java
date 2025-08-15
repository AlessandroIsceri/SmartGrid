package com.ii.smartgrid.smartgrid.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

public class Battery {
	
	private double voltage;
	private double maxCapacityInWatt;
	private double maxCapacityInAmp;
	private double storedEnergy;
    private double dischargeCurrent;
    private double efficiency;
    // double between 0 and 1 indicating the battery percentage
    private double stateOfCharge; 
	
	public Battery() {
        super();
    }

    public Battery(double maxCapacityInWatt, double maxCapacityInAmp, double storedEnergy, double voltage, double dischargeCurrent, double efficiency) {
        super();
        this.maxCapacityInWatt = maxCapacityInWatt;
        this.maxCapacityInAmp = maxCapacityInAmp;
        this.storedEnergy = storedEnergy;
        this.voltage = voltage;
        this.dischargeCurrent = dischargeCurrent;
        this.efficiency = efficiency;
        this.stateOfCharge = storedEnergy / maxCapacityInWatt;
    }

    public double getMaxCapacityInWatt() {
        return maxCapacityInWatt;
    }

    public void setMaxCapacityInWatt(double maxCapacityInWatt) {
        this.maxCapacityInWatt = maxCapacityInWatt;
        this.maxCapacityInAmp =  maxCapacityInWatt / voltage;
    }

    public double getMaxCapacityInAmp() {
        return maxCapacityInAmp;
    }

    public void setMaxCapacityInAmp(double maxCapacityInAmp) {
        this.maxCapacityInAmp = maxCapacityInAmp;
        this.maxCapacityInWatt = maxCapacityInAmp * voltage;
    }

    public double getVoltage() {
        return voltage;
    }


    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }


    public double getDischargeCurrent() {
        return dischargeCurrent;
    }


    public void setDischargeCurrent(double dischargeCurrent) {
        this.dischargeCurrent = dischargeCurrent;
    }


    public double getEfficiency() {
        return efficiency;
    }


    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }
	
	public double getStoredEnergy() {
		return storedEnergy;
	}
	
	
	public void setStoredEnergy(double storedEnergy) {
		this.storedEnergy = storedEnergy;
	}


    @JsonIgnore
    public double getMaxEnergyInTurn(){
        return voltage * dischargeCurrent * efficiency * TimeUtils.getTurnDurationHours(); //in Watts Hour
    }

    public double requestEnergy(double requestedEnergyWH){
        double maxEnergyInTurn = getMaxEnergyInTurn(); //in Watts Hour
        double effectiveRequestedEnergy = Math.min(maxEnergyInTurn, requestedEnergyWH);

        double remainingEnergy = storedEnergy - effectiveRequestedEnergy;

        if(remainingEnergy >= 0){
            storedEnergy -= effectiveRequestedEnergy;
            stateOfCharge = storedEnergy / maxCapacityInWatt;
            return effectiveRequestedEnergy;
        } else {
            double oldStored = storedEnergy;
            storedEnergy = 0.0;
            stateOfCharge = 0.0;
            return oldStored;
        }

    }
    

	public double fillBattery(double energyWH){

        //1000W -> 1 ora -> 1000WH
        //1000 -> 0:15 min -> 1000*0.25 WH
        double maxEnergyInTurn = getMaxEnergyInTurn(); //in Watts Hour

        //maxEnergy = 500; energyWH = 800 -> 500
        double effectiveFillEnergy = Math.min(maxEnergyInTurn, energyWH);
		double residual = Math.max(0.0, energyWH - maxEnergyInTurn);

		if(storedEnergy + effectiveFillEnergy <= maxCapacityInWatt){
			storedEnergy += effectiveFillEnergy;
            stateOfCharge = storedEnergy / maxCapacityInWatt;
			return residual;
		} else {
			double excess = storedEnergy + effectiveFillEnergy - maxCapacityInWatt;
			storedEnergy = maxCapacityInWatt;
            stateOfCharge = 1.0;
			return residual + excess;
		}
	}

    public double getStateOfCharge() {
        return stateOfCharge;
    }

    @JsonIgnore
    public double getAvailableEnergy() {
        return Math.min(getMaxEnergyInTurn(), storedEnergy);
    }
}
