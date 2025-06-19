package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;

import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class RequestEnergyBehaviour extends OneShotBehaviour{

    public RequestEnergyBehaviour(Grid grid) {
        super(grid);
    }

    @Override
    public void action() {
        String loadManagerName = ((Grid) myAgent).getLoadManagerName();
        HashMap<String, Object> content = new HashMap<String, Object>();
        if(((Grid) myAgent).getExpectedConsumption() < 0){
            content.put(MessageUtil.REQUESTED_ENERGY, 0.0);
        }else{
            content.put(MessageUtil.REQUESTED_ENERGY, ((Grid) myAgent).getExpectedConsumption());
        }
        ((Grid) myAgent).createAndSend(ACLMessage.REQUEST, loadManagerName, content);
    }

}
