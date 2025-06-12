package com.II.smartGrid.smartGrid.model;

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
	
	
	public double fillBattery(double energy){
		if(storedEnergy + energy <= maxCapacity){
			storedEnergy += energy;
			return 0;
		}
		else {
			double excess = energy - (maxCapacity - storedEnergy);
			storedEnergy = maxCapacity;
			return excess;
		}
	}
	
	
	@Override
	public String toString() {
		return "Battery [maxCapacity=" + maxCapacity + ", storedEnergy=" + storedEnergy + "]";
	}
	
	
	
	
}
