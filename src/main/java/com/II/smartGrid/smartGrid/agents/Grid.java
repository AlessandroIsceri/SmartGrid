package com.II.smartGrid.smartGrid.agents;

import java.util.List;

import com.II.smartGrid.smartGrid.agents.PowerPlant.Status;
import jade.core.Agent;

public class Grid extends Agent{

	private double currentEnergy;
	private double maxCapacity;
	private List<SmartHome> smartHomes;
	private List<ElectricVehicle> electricVehicles;
	
	@Override
    public void setup() {
		this.maxCapacity = (double) this.getArguments()[0];
        this.currentEnergy = 0.0;
        
	}
	
	public void addEnergy(double newEnergy) {
		if(currentEnergy + newEnergy < maxCapacity)
			currentEnergy = currentEnergy + newEnergy;
		else
			currentEnergy = maxCapacity;
	}

	public double getCurrentEnergy() {
		return currentEnergy;
	}

	public void setCurrentEnergy(double currentEnergy) {
		this.currentEnergy = currentEnergy;
	}

	public double getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public List<SmartHome> getSmartHomes() {
		return smartHomes;
	}

	public void setSmartHomes(List<SmartHome> smartHomes) {
		this.smartHomes = smartHomes;
	}

	public List<ElectricVehicle> getElectricVehicles() {
		return electricVehicles;
	}

	public void setElectricVehicles(List<ElectricVehicle> electricVehicles) {
		this.electricVehicles = electricVehicles;
	}

	@Override
	public String toString() {
		return "Grid [currentEnergy=" + currentEnergy + ", maxCapacity=" + maxCapacity + ", smartHomes=" + smartHomes
				+ ", electricVehicles=" + electricVehicles + "]";
	}
	
}
