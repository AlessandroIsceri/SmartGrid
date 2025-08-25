package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendNonRenewableEnergyToGridBehaviour;
import com.ii.smartgrid.behaviours.powerplant.UpdateNonRenewablePowerPlantStateBehaviour;
import com.ii.smartgrid.model.entities.NonRenewablePowerPlant;

import jade.core.behaviours.SequentialBehaviour;

public abstract class NonRenewablePowerPlantAgent extends PowerPlantAgent{

    public enum NonRenewablePowerPlantState {OFF, ON}
    protected NonRenewablePowerPlantState nonRenewablePowerPlantState;

    public NonRenewablePowerPlant getNonRenewablePowerPlant() {
        return (NonRenewablePowerPlant) referencedObject;
    }

    public NonRenewablePowerPlantState getNonRenewablePowerPlantState() {
        return nonRenewablePowerPlantState;
    }

    public void setNonRenewablePowerPlantState(NonRenewablePowerPlantState nonRenewablePowerPlantState) {
        this.nonRenewablePowerPlantState = nonRenewablePowerPlantState;
    }

    protected class NonRenewablePowerPlantBehaviour extends GenericTurnBehaviour{

        private NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent;

        public NonRenewablePowerPlantBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent){
            super(nonRenewablePowerPlantAgent);
            this.nonRenewablePowerPlantAgent = nonRenewablePowerPlantAgent;
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            // If it is active, send the produced energy to the connected grid
            NonRenewablePowerPlant nonRenewablePowerPlant = nonRenewablePowerPlantAgent.getNonRenewablePowerPlant();
            nonRenewablePowerPlant.setUpNonRenewableEnergyProduction(nonRenewablePowerPlantAgent.getCurTurn());
            if(nonRenewablePowerPlantState == NonRenewablePowerPlantState.ON){
                sequentialTurnBehaviour.addSubBehaviour(new SendNonRenewableEnergyToGridBehaviour(nonRenewablePowerPlantAgent));
            }
            sequentialTurnBehaviour.addSubBehaviour(new UpdateNonRenewablePowerPlantStateBehaviour(nonRenewablePowerPlantAgent));
        }

    }
}
