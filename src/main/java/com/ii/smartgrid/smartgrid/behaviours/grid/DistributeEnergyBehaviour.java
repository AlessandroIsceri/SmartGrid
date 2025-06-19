package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.Grid;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class DistributeEnergyBehaviour extends OneShotBehaviour{
    public DistributeEnergyBehaviour(Grid grid){
        super(grid);
    }

    @Override
    public void action() {
        Map<String, Double> smartHomesEnergyRequests = ((Grid) myAgent).getSmartHomesEnergyRequests();
        ((Grid) myAgent).log(smartHomesEnergyRequests.toString());
        double currentEnergy = ((Grid) myAgent).getCurrentEnergy();
        for(String smartHomeName : smartHomesEnergyRequests.keySet()){
            double requestedEnergy = smartHomesEnergyRequests.get(smartHomeName); 
            if(currentEnergy >= requestedEnergy){
                currentEnergy -= requestedEnergy;
                ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
                msg.addReceiver(new AID(smartHomeName, AID.ISLOCALNAME));
                msg.setContent("{\"operation\": \"consume\", \"energy\":" + requestedEnergy + "}");
                myAgent.send(msg);
            }else{
                ACLMessage msg = new ACLMessage(ACLMessage.REFUSE);
                msg.addReceiver(new AID(smartHomeName, AID.ISLOCALNAME));
                msg.setContent("{\"operation\": \"consume\", \"energy\":" + requestedEnergy + "}");
                myAgent.send(msg);
                ((Grid) myAgent).addSmartHomeWithoutPower(smartHomeName, requestedEnergy);
                ((Grid) myAgent).removeEnergyRequest(smartHomeName);
            }
        }
        ((Grid) myAgent).setCurrentEnergy(currentEnergy);
    }
}