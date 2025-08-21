package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendNonRenewableEnergyToGridBehaviour;
import com.ii.smartgrid.behaviours.powerplant.UpdateNonRenewableStatusBehaviour;
import com.ii.smartgrid.model.entities.NonRenewablePowerPlant;
import com.ii.smartgrid.utils.EnergyMonitorUtil;

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
            // If it is active, send the produced energy to the connected grid
            NonRenewablePowerPlant nonRenewablePowerPlant = nonRenewablePowerPlantAgent.getNonRenewablePowerPlant();
            EnergyMonitorUtil.addNonRenewableEnergyProduction(0, nonRenewablePowerPlantAgent.getCurTurn());
            if(nonRenewablePowerPlant.isOn()){
                sequentialTurnBehaviour.addSubBehaviour(new SendNonRenewableEnergyToGridBehaviour(nonRenewablePowerPlantAgent));
            }
            sequentialTurnBehaviour.addSubBehaviour(new UpdateNonRenewableStatusBehaviour(nonRenewablePowerPlantAgent));
        }

    }
}
