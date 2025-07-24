package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.RenewablePowerPlant;

import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public abstract class RenewablePowerPlantAgent extends PowerPlantAgent{
    // protected RenewablePowerPlant renewablePowerPlant;

    public RenewablePowerPlant getRenewablePowerPlant() {
        return (RenewablePowerPlant) referencedObject;
    }

    protected class RenewablePowerPlantBehaviour extends GenericTurnBehaviour {

        public RenewablePowerPlantBehaviour(RenewablePowerPlantAgent renewablePowerPlantAgent) {
            super(renewablePowerPlantAgent);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            SendProducedEnergyToLoadManagerBehaviour sendEnergyBehaviour = createSendEnergyBehaviourBehaviour();
            sequentialTurnBehaviour.addSubBehaviour(sendEnergyBehaviour);    
        }
    }

    protected abstract SendProducedEnergyToLoadManagerBehaviour createSendEnergyBehaviourBehaviour();

}
