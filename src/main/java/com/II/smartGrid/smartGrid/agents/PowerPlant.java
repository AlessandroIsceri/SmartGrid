package com.II.smartGrid.smartGrid.agents;

import com.II.smartGrid.smartGrid.behaviours.PowerPlantDistributeEnergy;

import java.util.List;

import com.II.smartGrid.smartGrid.behaviours.ProduceEnergy;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;

public class PowerPlant extends Agent {
	
	public enum Status {ON, OFF, MAINTENANCE};
	private Status status;
	private double maxCapacity;
	private double hProduction;
	private double storedEnergy;
	private List<Grid> grids;
	
	@Override
    public void setup() {
        status = Status.ON;
        maxCapacity = (double) this.getArguments()[0];
        hProduction = (double) this.getArguments()[1];
        //addBehaviour(new ProduceEnergy(this));
        //addBehaviour(new DistributeEnergy(this));
        
        ParallelBehaviour produceAndDistribute = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
        produceAndDistribute.addSubBehaviour(new ProduceEnergy(this));
        produceAndDistribute.addSubBehaviour(new PowerPlantDistributeEnergy(this));
        
    }

	public Status getStatus() {
		return status;
	}

	public double getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public double getHProduction() {
		return hProduction;
	}

	public void setHProduction(double h_production) {
		this.hProduction = h_production;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public double getStoredEnergy() {
		return storedEnergy;
	}

	public void setStoredEnergy(double currentCapacity) {
		this.storedEnergy = currentCapacity;
	}

	public List<Grid> getGrids() {
		return grids;
	}

	public void setGrids(List<Grid> grids) {
		this.grids = grids;
	}

	@Override
	public String toString() {
		return "PowerPlant [status=" + status + ", maxCapacity=" + maxCapacity + ", hProduction=" + hProduction
				+ ", storedEnergy=" + storedEnergy + ", grids=" + grids + "]";
	}
	
}

