package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.Grid;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


public class RestoreHomesBehaviour extends OneShotBehaviour {

    public RestoreHomesBehaviour(Grid grid){
        super(grid);
    }

    @Override
    public void action() {
        double currentEnergy = ((Grid) myAgent).getCurrentEnergy();
        double maxCapacity = ((Grid) myAgent).getMaxCapacity();
        if(maxCapacity * 0.5 < currentEnergy){
            // The grid has enough energy so it can be distributed
            Map<String, Double> receivers = ((Grid) myAgent).getSmartHomesWithoutPower();
            for(String receiver : receivers.keySet()){
                double requestedEnergy = receivers.get(receiver);
                if(requestedEnergy > currentEnergy){
                    ((Grid) myAgent).consumeEnergy(requestedEnergy);
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
                    msg.setConversationId("restore-" + receiver);
                    msg.setContent("{\"energy\": " + requestedEnergy + "}");
                    myAgent.send(msg);
                    ((Grid) myAgent).removeSmartHomeWithoutPower(receiver);
                }
            }
        }

        Map<String, Double> receivers = ((Grid) myAgent).getSmartHomesWithoutPower();
        ((Grid) myAgent).log(receivers.toString());
        for(String receiver : receivers.keySet()){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
            msg.setConversationId("restore-" + receiver);
            msg.setContent("{\"energy\": " + -1 + "}");
            myAgent.send(msg);
        }

    }
}