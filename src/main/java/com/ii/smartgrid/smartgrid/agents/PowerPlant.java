package com.ii.smartgrid.smartgrid.agents;

import java.util.List;

import com.ii.smartgrid.smartgrid.behaviours.powerplant.PowerPlantDistributeEnergy;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.ProduceEnergy;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;

public class PowerPlant extends CustomAgent {
	
	public enum PPStatus {ON, OFF, MAINTENANCE};
	private PPStatus status;
	private double maxCapacity;
	private double hProduction;
	private double storedEnergy;
	private List<Grid> grids;
	
	@Override
    public void setup() {
        status = PPStatus.ON;
        maxCapacity = (double) this.getArguments()[0];
        hProduction = (double) this.getArguments()[1];
        //addBehaviour(new ProduceEnergy(this));
        //addBehaviour(new DistributeEnergy(this));
        
        this.log("Setup completed");
        ParallelBehaviour produceAndDistribute = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
        produceAndDistribute.addSubBehaviour(new ProduceEnergy(this));
        produceAndDistribute.addSubBehaviour(new PowerPlantDistributeEnergy(this));
        
    }

	public PPStatus getStatus() {
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

	public void setStatus(PPStatus status) {
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

