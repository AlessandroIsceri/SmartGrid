package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.DistributeNonRenewableEnergy;
import com.ii.smartgrid.smartgrid.model.Battery;

import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class NonRenewablePowerPlant extends PowerPlant{

    public static String[] TYPES = {"gas", "diesel", "coal"};
    private double hProduction;
    
    public void setup(){
        status = PPStatus.ON;
        Object[] args = this.getArguments();
       
        loadManagerName = (String) args[args.length - 4];
        hProduction = Double.parseDouble((String) args[args.length - 3]);
        double maxCapacity = Double.parseDouble((String) args[args.length - 2]);
        double storedEnergy = Double.parseDouble((String) args[args.length - 1]);
        
        battery = new Battery(maxCapacity, storedEnergy);
                
        addBehaviour(new NonRenewablePowerPlantBehaviour(this));
        this.log("Setup completed");        
     
    }


    public double gethProduction() {
        return hProduction;
    }


    private class NonRenewablePowerPlantBehaviour extends GenericTurnBehaviour{

        public NonRenewablePowerPlantBehaviour(NonRenewablePowerPlant nonRenewablePowerPlant){
            super(nonRenewablePowerPlant);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            //riceve un messaggio con la richiesta di energia
            //risponde inviando l'energia richiesta
            
            sequentialTurnBehaviour.addSubBehaviour(new DistributeNonRenewableEnergy((NonRenewablePowerPlant) myAgent));




            // double hProduction = ((PowerPlant) myAgent).getHProduction();
            // int turnDuration = TimeUtils.getTurnDuration();
            // double turnProduction = hProduction / 60 * turnDuration;
            // ((PowerPlant) myAgent).setCurTurnExpectedProduction(turnProduction);
        }

    }
}
