package com.ii.smartgrid.behaviours.simulation;

import com.ii.smartgrid.agents.SimulationAgent;
import com.ii.smartgrid.agents.SimulationAgent.SimulationState;
import com.ii.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.utils.MessageUtil;

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
        MessageTemplate or = MessageTemplate.or(MessageTemplate.MatchConversationId(MessageUtil.CONVERSATION_ID_STOP_SIMULATION), 
                                                MessageTemplate.MatchConversationId(MessageUtil.CONVERSATION_ID_RESUME_SIMULATION));
        MessageTemplate or1 = MessageTemplate.or(or, MessageTemplate.MatchConversationId(MessageUtil.CONVERSATION_ID_START_SIMULATION));
        MessageTemplate mt = MessageTemplate.and(or1, MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        ACLMessage receivedMessage = customAgent.receive(mt);
		if (receivedMessage != null) {
            if(receivedMessage.getConversationId().equals(MessageUtil.CONVERSATION_ID_RESUME_SIMULATION)){
                simulationAgent.setSimulationState(SimulationState.ON);
                log("Simulation resumed");
                
                // Send new turn message
                simulationAgent.updateTurn();
                System.out.println("\n\n\n");
                log("Started new turn");
                simulationAgent.sendMessages();
                
            } else if(receivedMessage.getConversationId().equals(MessageUtil.CONVERSATION_ID_STOP_SIMULATION)){
                simulationAgent.setSimulationState(SimulationState.OFF);
                log("Simulation blocked");
            } else if(receivedMessage.getConversationId().equals(MessageUtil.CONVERSATION_ID_START_SIMULATION)) {
				simulationAgent.setSimulationState(SimulationState.ON);
				simulationAgent.sendMessages();
				log("Started first turn");
            }
            block();
		}else {
			block();
		}
            
    }

}
