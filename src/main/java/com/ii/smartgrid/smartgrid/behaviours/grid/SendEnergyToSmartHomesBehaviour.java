package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyToSmartHomesBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendEnergyToSmartHomesBehaviour(GridAgent gridAgent){
        super(gridAgent);
    }

    @Override
    public void action() {
        Grid grid = ((GridAgent) myAgent).getGrid();
        Map<String, Double> smartHomesEnergyRequests = grid.getSmartHomesEnergyRequests();
        ((CustomAgent) myAgent).log(smartHomesEnergyRequests.toString(), BEHAVIOUR_NAME);
        double currentEnergy = grid.getCurrentEnergy();
        for(String smartHomeName : smartHomesEnergyRequests.keySet()){
            double requestedEnergy = smartHomesEnergyRequests.get(smartHomeName); 
            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.OPERATION, MessageUtil.CONSUME);
            content.put(MessageUtil.REQUESTED_ENERGY, requestedEnergy);
            if(currentEnergy >= requestedEnergy){
                currentEnergy -= requestedEnergy;
                ((CustomAgent) myAgent).createAndSend(ACLMessage.AGREE, smartHomeName, content);
            }else{
                ((CustomAgent) myAgent).createAndSend(ACLMessage.REFUSE, smartHomeName, content);
                grid.addSmartHomeWithoutPower(smartHomeName, requestedEnergy);
            }
            grid.removeEnergyRequest(smartHomeName);
        }
        grid.setCurrentEnergy(currentEnergy);
    }
}