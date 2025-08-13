package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendNonRenewableEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.UpdateNonRenewableStatus;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;

import jade.core.behaviours.SequentialBehaviour;

public abstract class NonRenewablePowerPlantAgent extends PowerPlantAgent{

    // protected NonRenewablePowerPlant nonRenewablePowerPlant;

    public NonRenewablePowerPlant getNonRenewablePowerPlant() {
        return (NonRenewablePowerPlant) referencedObject;
    }

    protected class NonRenewablePowerPlantBehaviour extends GenericTurnBehaviour{

        public NonRenewablePowerPlantBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent){
            super(nonRenewablePowerPlantAgent);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            //se è attivo, invia l'energia prodotta
            NonRenewablePowerPlant nonRenewablePowerPlant = ((NonRenewablePowerPlantAgent) myAgent).getNonRenewablePowerPlant();
            if(nonRenewablePowerPlant.isOn()){
                sequentialTurnBehaviour.addSubBehaviour(new SendNonRenewableEnergyToGridBehaviour((NonRenewablePowerPlantAgent) myAgent));
            }
            sequentialTurnBehaviour.addSubBehaviour(new UpdateNonRenewableStatus((NonRenewablePowerPlantAgent) myAgent));
        }

    }
}
