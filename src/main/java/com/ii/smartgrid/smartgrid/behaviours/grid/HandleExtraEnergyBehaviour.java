package com.ii.smartgrid.smartgrid.behaviours.grid;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;

import jade.core.behaviours.OneShotBehaviour;

public class HandleExtraEnergyBehaviour extends CustomOneShotBehaviour{
    
    private GridAgent gridAgent;

    public HandleExtraEnergyBehaviour(GridAgent gridAgent){
        super(gridAgent);
        this.gridAgent = gridAgent;
    }

    @Override
    public void action() {
        Grid grid = gridAgent.getGrid();
        double extraEnergy = grid.getExpectedProduction() - grid.getExpectedConsumption();
        if(grid.getBattery() != null){
            double energyLost = grid.fillBattery(extraEnergy);
            log("Energy lost: " + energyLost);
        }
        grid.setExpectedConsumption(grid.getBlackoutEnergyRequest());

        //TODO:REMOVE
        grid.resetValues();
        log("*****" + grid.toString());

    }    

}
