package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendProducedEnergyToGridBehaviour;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;

import jade.core.behaviours.SequentialBehaviour;

public abstract class RenewablePowerPlantAgent extends PowerPlantAgent{

    public RenewablePowerPlant getRenewablePowerPlant() {
        return (RenewablePowerPlant) referencedObject;
    }

    protected class RenewablePowerPlantBehaviour extends GenericTurnBehaviour {

        public RenewablePowerPlantBehaviour(RenewablePowerPlantAgent renewablePowerPlantAgent) {
            super(renewablePowerPlantAgent);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            // Send the produced energy to the connected grid
            SendProducedEnergyToGridBehaviour sendEnergyBehaviour = createSendEnergyBehaviourBehaviour();
            sequentialTurnBehaviour.addSubBehaviour(sendEnergyBehaviour);    
        }
    }

    protected abstract SendProducedEnergyToGridBehaviour createSendEnergyBehaviourBehaviour();

}
