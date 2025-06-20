package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyRequestToLoadManagerBehaviour extends OneShotBehaviour{

    public SendEnergyRequestToLoadManagerBehaviour(Grid grid) {
        super(grid);
    }

    @Override
    public void action() {
        String loadManagerName = ((Grid) myAgent).getLoadManagerName();
        Map<String, Object> content = new HashMap<String, Object>();
        if(((Grid) myAgent).getExpectedConsumption() < 0){
            content.put(MessageUtil.REQUESTED_ENERGY, 0.0);
        }else{
            content.put(MessageUtil.REQUESTED_ENERGY, ((Grid) myAgent).getExpectedConsumption());
        }
        ((Grid) myAgent).createAndSend(ACLMessage.REQUEST, loadManagerName, content);
    }

}
