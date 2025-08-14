package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendNonRenewableEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.UpdateNonRenewableStatusBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.UpdateNonRenewableStatusBehaviour;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;

import jade.core.behaviours.SequentialBehaviour;

public abstract class NonRenewablePowerPlantAgent extends PowerPlantAgent{

    public NonRenewablePowerPlant getNonRenewablePowerPlant() {
        return (NonRenewablePowerPlant) referencedObject;
    }

    protected class NonRenewablePowerPlantBehaviour extends GenericTurnBehaviour{

        private NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent;

        public NonRenewablePowerPlantBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent){
            super(nonRenewablePowerPlantAgent);
            this.nonRenewablePowerPlantAgent = nonRenewablePowerPlantAgent;
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            //se è attivo, invia l'energia prodotta
            NonRenewablePowerPlant nonRenewablePowerPlant = nonRenewablePowerPlantAgent.getNonRenewablePowerPlant();
            if(nonRenewablePowerPlant.isOn()){
                sequentialTurnBehaviour.addSubBehaviour(new SendNonRenewableEnergyToGridBehaviour(nonRenewablePowerPlantAgent));
            }
            sequentialTurnBehaviour.addSubBehaviour(new UpdateNonRenewableStatusBehaviour(nonRenewablePowerPlantAgent));
        }

    }
}
