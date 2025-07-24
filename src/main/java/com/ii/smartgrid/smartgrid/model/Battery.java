package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;

public class Battery {
	private double maxCapacity;
	private double storedEnergy;
	
	public Battery(double maxCapacity, double storedEnergy) {
		super();
		this.maxCapacity = maxCapacity;
		this.storedEnergy = storedEnergy;
	}
	
	
	public Battery() {
		super();
	}
	
	public double getMaxCapacity() {
		return maxCapacity;
	}
	
	
	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
	
	
	public double getStoredEnergy() {
		return storedEnergy;
	}
	
	
	public void setStoredEnergy(double storedEnergy) {
		this.storedEnergy = storedEnergy;
	}
	
    public double requestEnergy(double requestedEnergy){
        double requestedEnergyWH = requestedEnergy * TimeUtils.getTurnDurationHours(); //convert Watts in Watts Hour
        if(storedEnergy >= requestedEnergyWH){
            storedEnergy -= requestedEnergyWH;
            return requestedEnergy;
        }else{
            double oldStored = storedEnergy;
            storedEnergy = 0;
            return oldStored;
        }
    }
	
	public double fillBattery(double energy){

        double energyWH = energy * TimeUtils.getTurnDurationHours(); //convert Watts in Watts Hour

		if(storedEnergy + energyWH <= maxCapacity){
			storedEnergy += energyWH;
			return 0;
		}
		else {
			double excess = energy - (maxCapacity - energyWH);
			storedEnergy = maxCapacity;
			return excess;
		}
	}
	
	
	@Override
	public String toString() {
		return "Battery [maxCapacity=" + maxCapacity + ", storedEnergy=" + storedEnergy + "]";
	}
	
	
	
	
}
