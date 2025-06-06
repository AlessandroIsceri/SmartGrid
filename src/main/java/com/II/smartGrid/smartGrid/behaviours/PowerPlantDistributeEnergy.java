package com.II.smartGrid.smartGrid.behaviours;

import java.util.ArrayList;
import java.util.List;

import com.II.smartGrid.smartGrid.agents.Grid;
import com.II.smartGrid.smartGrid.agents.PowerPlant;

import jade.core.behaviours.CyclicBehaviour;

public class PowerPlantDistributeEnergy extends CyclicBehaviour{

	private PowerPlant powerPlant;
	
	public PowerPlantDistributeEnergy(PowerPlant powerPlant) {
		this.powerPlant = powerPlant;
	}
	
	@Override
	public void action() {
		List<Grid> grids = powerPlant.getGrids();
		int n = grids.size();
		double storedEnergy = powerPlant.getStoredEnergy();
		double energyPerGrid = storedEnergy / n;
		
		for(int i = 0; i < n; i++) {
			grids.get(i).addEnergy(energyPerGrid);
		}
		
	}

}
