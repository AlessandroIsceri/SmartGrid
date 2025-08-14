package com.ii.smartgrid.smartgrid.utils;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.smartgrid.utils.SimulationSettingsAgent.SimulationStatus;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckSimulationSettingsMessages extends CustomCyclicBehaviour{

    private SimulationSettingsAgent simulationSettingsAgent;

    public CheckSimulationSettingsMessages(SimulationSettingsAgent simulationSettingsAgent){
        super(simulationSettingsAgent);
        this.simulationSettingsAgent = simulationSettingsAgent;
    }

    @Override
    public void action() {
        MessageTemplate or = MessageTemplate.or(MessageTemplate.MatchConversationId("stop-simulation"), 
                                                MessageTemplate.MatchConversationId("resume-simulation"));
        MessageTemplate or1 = MessageTemplate.or(or, MessageTemplate.MatchConversationId("start-simulation"));
        MessageTemplate mt = MessageTemplate.and(or1, MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        ACLMessage receivedMessage = customAgent.receive(mt);
		if (receivedMessage != null) {
            log(this.getBehaviourName() + " RECEIVED A MESSAGE FROM " + receivedMessage.getSender().getLocalName());
            if(receivedMessage.getConversationId().contains("resume")){
                simulationSettingsAgent.setSimulationStatus(SimulationStatus.ON);
                log("SimulationSettings resumed");
                
                //send new turn message
                simulationSettingsAgent.updateTurn();
                System.out.println("\n\n\n");
                log("Started new turn");
                simulationSettingsAgent.sendMessages();
                
            } else if(receivedMessage.getConversationId().contains("stop")){
                simulationSettingsAgent.setSimulationStatus(SimulationStatus.OFF);
                log("SimulationSettings blocked");
                
            } else if(receivedMessage.getConversationId().contains("start")) {
				simulationSettingsAgent.setSimulationStatus(SimulationStatus.ON);
				simulationSettingsAgent.sendMessages();
				log("Started first turn");
            }
            block();
		}else {
			block();
		}
            
    }

}
