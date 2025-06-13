package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.ArrayList;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.agents.PowerPlant;

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
