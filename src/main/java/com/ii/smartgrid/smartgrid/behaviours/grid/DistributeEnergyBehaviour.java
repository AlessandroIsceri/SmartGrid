package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

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
            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.OPERATION, MessageUtil.CONSUME);
            content.put(MessageUtil.REQUESTED_ENERGY, requestedEnergy);
            if(currentEnergy >= requestedEnergy){
                currentEnergy -= requestedEnergy;
                ((Grid) myAgent).createAndSend(ACLMessage.AGREE, smartHomeName, content);
            }else{
                ((Grid) myAgent).createAndSend(ACLMessage.REFUSE, smartHomeName, content);
                ((Grid) myAgent).addSmartHomeWithoutPower(smartHomeName, requestedEnergy);
                ((Grid) myAgent).removeEnergyRequest(smartHomeName);
            }
        }
        ((Grid) myAgent).setCurrentEnergy(currentEnergy);
    }
}