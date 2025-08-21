package com.ii.smartgrid.behaviours.grid;

import com.ii.smartgrid.agents.GridAgent;
import com.ii.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.model.entities.Grid;    
import com.ii.smartgrid.utils.EnergyMonitorUtil;

public class HandleExtraEnergyBehaviour extends CustomOneShotBehaviour {

    private GridAgent gridAgent;

    public HandleExtraEnergyBehaviour(GridAgent gridAgent) {
        super(gridAgent);
        this.gridAgent = gridAgent;
    }

    @Override
    public void action() {
        Grid grid = gridAgent.getGrid();
        double extraEnergy = grid.getExpectedProduction() - grid.getExpectedConsumption();
        if (grid.getBattery() != null) {
            if(extraEnergy > 0){
                double energyLost = grid.fillBattery(extraEnergy);
                if (energyLost > 0) {
                    log("Energy lost: " + energyLost);
                }
            }
        }
        
        
        grid.resetValues();
        grid.setExpectedConsumption(grid.getBlackoutEnergyRequest());
    }

}
