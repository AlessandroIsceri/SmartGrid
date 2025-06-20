package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.ReceiveNonRenewableEnergyRequestFromLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendNonRenewableEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;

import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class NonRenewablePowerPlant extends PowerPlant{

    public static String[] TYPES = {"gas", "diesel", "coal"};
    private double hourlyProduction;
    private double requestedEnergy;
    
    public void setup(){
        status = PPStatus.ON;
        Object[] args = this.getArguments();
       
        loadManagerName = (String) args[0];
        hourlyProduction = Double.parseDouble((String) args[1]);
        double maxCapacity = Double.parseDouble((String) args[2]);
        double storedEnergy = Double.parseDouble((String) args[3]);
        
        battery = new Battery(maxCapacity, storedEnergy);
                
        addBehaviour(new NonRenewablePowerPlantBehaviour(this));
        this.log("Setup completed");        
     
    }


    public double getHourlyProduction() {
        return hourlyProduction;
    }

    public double getRequestedEnergy() {
        return requestedEnergy;
    }


    public void setRequestedEnergy(double requestedEnergy) {
        this.requestedEnergy = requestedEnergy;
    }

    private class NonRenewablePowerPlantBehaviour extends GenericTurnBehaviour{

        public NonRenewablePowerPlantBehaviour(NonRenewablePowerPlant nonRenewablePowerPlant){
            super(nonRenewablePowerPlant);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            //riceve un messaggio con la richiesta di energia
            //risponde inviando l'energia richiesta
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveNonRenewableEnergyRequestFromLoadManagerBehaviour((NonRenewablePowerPlant) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendNonRenewableEnergyToLoadManagerBehaviour((NonRenewablePowerPlant) myAgent));

        }

    }
}
