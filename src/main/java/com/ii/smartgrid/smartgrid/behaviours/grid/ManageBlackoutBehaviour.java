package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.ArrayList;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.agents.SmartHome;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ManageBlackoutBehaviour extends CyclicBehaviour{

    public ManageBlackoutBehaviour(Grid grid){
        super(grid);
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                                                 MessageTemplate.MatchContent("{\"blackout\": true}"));
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((Grid) myAgent).addSmartHomeWithoutPower(receivedMsg.getSender().getLocalName());
        }else{
            block();
        }
    }   
}