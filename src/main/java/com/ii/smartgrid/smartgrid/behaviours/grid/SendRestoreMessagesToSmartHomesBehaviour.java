package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


public class SendRestoreMessagesToSmartHomesBehaviour extends OneShotBehaviour {

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendRestoreMessagesToSmartHomesBehaviour(GridAgent gridAgent){
        super(gridAgent);
    }

    @Override
    public void action() {
        Grid grid = ((GridAgent) myAgent).getGrid();
        double currentEnergy = grid.getCurrentEnergy();
        double maxCapacity = grid.getMaxCapacity();
        if(maxCapacity * 0.5 < currentEnergy){
            // The grid has enough energy so it can be distributed
            Map<String, Double> receivers = grid.getSmartHomesWithoutPower();
            for(String receiverName : receivers.keySet()){
                double requestedEnergy = receivers.get(receiverName);
                if (grid.consumeEnergy(requestedEnergy)){
                    Map<String, Object> content = new HashMap<String, Object>();
                    content.put(MessageUtil.GIVEN_ENERGY, requestedEnergy);
                    ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, receiverName, content, "restore-" + receiverName);
                    grid.removeSmartHomeWithoutPower(receiverName);
                }    
            }
        }

        Map<String, Double> receivers = grid.getSmartHomesWithoutPower();
        ((CustomAgent) myAgent).log("SH without power:" + receivers.toString(), BEHAVIOUR_NAME);
        for(String receiverName : receivers.keySet()){

            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.GIVEN_ENERGY, -1.0);
            ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, receiverName, content, "restore-" + receiverName);
            grid.removeSmartHomeWithoutPower(receiverName);
        }

    }
}