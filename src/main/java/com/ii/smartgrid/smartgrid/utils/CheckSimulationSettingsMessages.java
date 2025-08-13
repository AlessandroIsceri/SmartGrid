package com.ii.smartgrid.smartgrid.utils;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.SimulationStatus;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckSimulationSettingsMessages extends CustomCyclicBehaviour{


    public CheckSimulationSettingsMessages(SimulationSettings simulationSettings){
        super(simulationSettings);
    }

    @Override
    public void action() {
        MessageTemplate or = MessageTemplate.or(MessageTemplate.MatchConversationId("stop-simulation"), 
                                                MessageTemplate.MatchConversationId("resume-simulation"));
        MessageTemplate or1 = MessageTemplate.or(or, MessageTemplate.MatchConversationId("start-simulation"));
        MessageTemplate mt = MessageTemplate.and(or1, MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        ACLMessage receivedMessage = myAgent.receive(mt);
		if (receivedMessage != null) {
            ((SimulationSettings) myAgent).log(this.getBehaviourName() + " RECEIVED A MESSAGE FROM " + receivedMessage.getSender().getLocalName(), BEHAVIOUR_NAME);
            if(receivedMessage.getConversationId().contains("resume")){
                ((SimulationSettings) myAgent).setSimulationStatus(SimulationStatus.ON);
                ((SimulationSettings) myAgent).log("SimulationSettings resumed", BEHAVIOUR_NAME);
                
                //send new turn message
                ((SimulationSettings) myAgent).updateTurn();
                System.out.println("\n\n\n");
                ((SimulationSettings) myAgent).log("Started new turn", BEHAVIOUR_NAME);
                ((SimulationSettings) myAgent).sendMessages();
                
            } else if(receivedMessage.getConversationId().contains("stop")){
                ((SimulationSettings) myAgent).setSimulationStatus(SimulationStatus.OFF);
                ((SimulationSettings) myAgent).log("SimulationSettings blocked", BEHAVIOUR_NAME);
                
            } else if(receivedMessage.getConversationId().contains("start")) {
				((SimulationSettings) myAgent).setSimulationStatus(SimulationStatus.ON);
				((SimulationSettings) myAgent).sendMessages();
				((SimulationSettings) myAgent).log("Started first turn", BEHAVIOUR_NAME);
            }
            block();
		}else {
			block();
		}
            
    }

}
