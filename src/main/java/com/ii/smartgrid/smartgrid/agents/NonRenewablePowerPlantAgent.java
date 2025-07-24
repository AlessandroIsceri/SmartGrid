package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.ReceiveNonRenewableEnergyRequestFromLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendNonRenewableEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;

import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

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
            //riceve un messaggio con la richiesta di energia
            //risponde inviando l'energia richiesta
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveNonRenewableEnergyRequestFromLoadManagerBehaviour((NonRenewablePowerPlantAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendNonRenewableEnergyToLoadManagerBehaviour((NonRenewablePowerPlantAgent) myAgent));

        }

    }
}
