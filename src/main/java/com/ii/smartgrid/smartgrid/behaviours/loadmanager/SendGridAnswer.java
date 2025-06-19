package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.LinkedHashMap;

import com.ii.smartgrid.smartgrid.agents.LoadManager;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendGridAnswer extends OneShotBehaviour{

    public SendGridAnswer(LoadManager loadManager){
        super(loadManager);
    }
    
    @Override
    public void action() {
        double expectedConsumption = ((LoadManager) myAgent).getExpectedConsumption();
        
        ACLMessage message = new ACLMessage(ACLMessage.AGREE);
        // "givenEnergy" : 2000
        // "neededEnergy": 2000

        LinkedHashMap<String, Double> gridRequestedEnergy = ((LoadManager) myAgent).getGridRequestedEnergy();
        for(String gridName : gridRequestedEnergy.keySet()){
            message.setContent("{\"givenEnergy\": " + (gridRequestedEnergy.get(gridName) - expectedConsumption) + ", \"neededEnergy\": " + gridRequestedEnergy.get(gridName) + "}");
            message.addReceiver(new AID(gridName, AID.ISLOCALNAME));
            myAgent.send(message);
        }
    }

}
