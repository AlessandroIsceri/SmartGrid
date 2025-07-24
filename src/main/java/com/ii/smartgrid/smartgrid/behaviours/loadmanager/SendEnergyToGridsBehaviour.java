package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyToGridsBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendEnergyToGridsBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }
    
    @Override
    public void action() {
        LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
        double expectedConsumption = loadManager.getExpectedConsumption();
        
        // "givenEnergy" : 2000
        // "neededEnergy": 2000        
        Map<String, Double> gridRequestedEnergy = loadManager.getGridRequestedEnergy();
        for(String gridName : gridRequestedEnergy.keySet()){
            
            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.GIVEN_ENERGY, (gridRequestedEnergy.get(gridName) - expectedConsumption));
            content.put(MessageUtil.NEEDED_ENERGY, gridRequestedEnergy.get(gridName));
            ((CustomAgent) myAgent).createAndSend(ACLMessage.AGREE, gridName, content);
        }
    }

}
