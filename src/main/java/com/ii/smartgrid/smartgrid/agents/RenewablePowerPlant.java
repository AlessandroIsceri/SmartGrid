package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedEnergyBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;

import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public abstract class RenewablePowerPlant extends PowerPlant{
    public static String[] TYPES = {"solar", "wind", "hydro"};

    public abstract double getHProduction();

    protected class RenewablePowerPlantBehaviour extends GenericTurnBehaviour {

        public RenewablePowerPlantBehaviour(RenewablePowerPlant renewablePowerPlant) {
            super(renewablePowerPlant);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            sequentialTurnBehaviour.addSubBehaviour(new SendProducedEnergyBehaviour((RenewablePowerPlant) myAgent));    
        }
    
        
    }

}
