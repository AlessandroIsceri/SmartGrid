package com.ii.smartgrid.smartgrid.behaviours.grid;

import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.entities.Grid;

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
            double energyLost = grid.fillBattery(extraEnergy);
            if (energyLost > 0) {
                log("Energy lost: " + energyLost);
            }
        }
        grid.resetValues();
        grid.setExpectedConsumption(grid.getBlackoutEnergyRequest());
    }

}
