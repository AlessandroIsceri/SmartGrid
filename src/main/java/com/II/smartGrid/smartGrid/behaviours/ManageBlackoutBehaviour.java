package com.II.smartGrid.smartGrid.behaviours;

import java.util.ArrayList;
import java.util.List;

import com.II.smartGrid.smartGrid.agents.Grid;
import com.II.smartGrid.smartGrid.agents.SmartHome;

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