package com.ii.smartgrid.smartgrid.behaviours.grid;

import com.ii.smartgrid.smartgrid.agents.Grid;

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
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        String loadManagerName = ((Grid) myAgent).getLoadManagerName();
        if(((Grid) myAgent).getExpectedConsumption() < 0){
            message.setContent("{\"energy\":" + 0.0 + "}");
        }else{
            message.setContent("{\"energy\":" + ((Grid) myAgent).getExpectedConsumption() + "}");
        }
        
        message.addReceiver(new AID(loadManagerName, AID.ISLOCALNAME));
        myAgent.send(message);
    }

}
