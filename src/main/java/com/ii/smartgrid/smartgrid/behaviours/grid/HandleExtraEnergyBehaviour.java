package com.ii.smartgrid.smartgrid.behaviours.grid;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.model.Grid;

import jade.core.behaviours.OneShotBehaviour;

public class HandleExtraEnergyBehaviour extends OneShotBehaviour{
    
    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public HandleExtraEnergyBehaviour(GridAgent gridAgent){
        super(gridAgent);
    }

    @Override
    public void action() {
        ((CustomAgent) myAgent).log("Started", BEHAVIOUR_NAME);
        Grid grid = ((GridAgent) myAgent).getGrid();
        double extraEnergy = grid.getExpectedProduction() - grid.getExpectedConsumption();
        if(grid.getBattery() != null){
            double energyLost = grid.fillBattery(extraEnergy);
            ((CustomAgent) myAgent).log("Energy lost: " + energyLost, BEHAVIOUR_NAME);
        }
        grid.setExpectedConsumption(grid.getBlackoutEnergyRequest());
        ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);


        //TODO:REMOVE
        grid.resetValues();
        ((CustomAgent) myAgent).log("*****" + grid.toString(), BEHAVIOUR_NAME);

    }    

}
