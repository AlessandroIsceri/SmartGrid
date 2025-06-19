package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.LinkedHashMap;
import java.util.HashMap;


import com.ii.smartgrid.smartgrid.agents.LoadManager;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

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
        
        // "givenEnergy" : 2000
        // "neededEnergy": 2000        
        LinkedHashMap<String, Double> gridRequestedEnergy = ((LoadManager) myAgent).getGridRequestedEnergy();
        for(String gridName : gridRequestedEnergy.keySet()){
            
            HashMap<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.GIVEN_ENERGY, (gridRequestedEnergy.get(gridName) - expectedConsumption));
            content.put(MessageUtil.NEEDED_ENERGY, gridRequestedEnergy.get(gridName));
            ((LoadManager) myAgent).createAndSend(ACLMessage.AGREE, gridName, content);
        }
    }

}
