package com.ii.smartgrid.behaviours.simulation;

import com.ii.smartgrid.agents.SimulationAgent;
import com.ii.smartgrid.agents.SimulationAgent.SimulationStatus;
import com.ii.smartgrid.behaviours.CustomCyclicBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckSimulationMessagesBehaviour extends CustomCyclicBehaviour{

    private SimulationAgent simulationAgent;

    public CheckSimulationMessagesBehaviour(SimulationAgent simulationAgent){
        super(simulationAgent);
        this.simulationAgent = simulationAgent;
    }

    @Override
    public void action() {
        MessageTemplate or = MessageTemplate.or(MessageTemplate.MatchConversationId("stop-simulation"), 
                                                MessageTemplate.MatchConversationId("resume-simulation"));
        MessageTemplate or1 = MessageTemplate.or(or, MessageTemplate.MatchConversationId("start-simulation"));
        MessageTemplate mt = MessageTemplate.and(or1, MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        ACLMessage receivedMessage = customAgent.receive(mt);
		if (receivedMessage != null) {
            if(receivedMessage.getConversationId().contains("resume")){
                simulationAgent.setSimulationStatus(SimulationStatus.ON);
                log("Simulation resumed");
                
                // Send new turn message
                simulationAgent.updateTurn();
                System.out.println("\n\n\n");
                log("Started new turn");
                simulationAgent.sendMessages();
                
            } else if(receivedMessage.getConversationId().contains("stop")){
                simulationAgent.setSimulationStatus(SimulationStatus.OFF);
                log("Simulation blocked");
            } else if(receivedMessage.getConversationId().contains("start")) {
				simulationAgent.setSimulationStatus(SimulationStatus.ON);
				simulationAgent.sendMessages();
				log("Started first turn");
            }
            block();
		}else {
			block();
		}
            
    }

}
