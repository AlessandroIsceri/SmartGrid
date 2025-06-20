package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


public class SendRestoreMessagesToSmartHomesBehaviour extends OneShotBehaviour {

    public SendRestoreMessagesToSmartHomesBehaviour(Grid grid){
        super(grid);
    }

    @Override
    public void action() {
        double currentEnergy = ((Grid) myAgent).getCurrentEnergy();
        double maxCapacity = ((Grid) myAgent).getMaxCapacity();
        if(maxCapacity * 0.5 < currentEnergy){
            // The grid has enough energy so it can be distributed
            Map<String, Double> receivers = ((Grid) myAgent).getSmartHomesWithoutPower();
            for(String receiverName : receivers.keySet()){
                double requestedEnergy = receivers.get(receiverName);
                if(((Grid) myAgent).consumeEnergy(requestedEnergy)){
                    Map<String, Object> content = new HashMap<String, Object>();
                    content.put(MessageUtil.GIVEN_ENERGY, requestedEnergy);
                    ((Grid) myAgent).createAndSend(ACLMessage.INFORM, receiverName, content, "restore-" + receiverName);
                    ((Grid) myAgent).removeSmartHomeWithoutPower(receiverName);
                }    
            }
        }

        Map<String, Double> receivers = ((Grid) myAgent).getSmartHomesWithoutPower();
        ((Grid) myAgent).log("SH without power:" + receivers.toString());
        for(String receiverName : receivers.keySet()){

            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.GIVEN_ENERGY, -1.0);
            ((Grid) myAgent).createAndSend(ACLMessage.INFORM, receiverName, content, "restore-" + receiverName);
            ((Grid) myAgent).removeSmartHomeWithoutPower(receiverName);
        }

    }
}