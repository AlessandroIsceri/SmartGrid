package com.ii.smartgrid.behaviours.simulation;

import com.ii.smartgrid.agents.SimulationAgent;
import com.ii.smartgrid.agents.SimulationAgent.SimulationState;
import com.ii.smartgrid.behaviours.CustomCyclicBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class StartNewTurnBehaviour extends CustomCyclicBehaviour {

	private int receivedAnswers = 0;
    private SimulationAgent simulationAgent;
	
	public StartNewTurnBehaviour(SimulationAgent simulationAgent) {
		super(simulationAgent);
        this.simulationAgent = (SimulationAgent) customAgent;
	}
	
	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
			if(receivedMsg.getPerformative() == ACLMessage.INFORM) {
				receivedAnswers++;
				int numberOfMessagesToReceive = simulationAgent.getAgentNames().size();
                log("Received answers: " + receivedAnswers + "/" + numberOfMessagesToReceive + ", last sender: " + receivedMsg.getSender().getLocalName());
				if(receivedAnswers == numberOfMessagesToReceive) {
					receivedAnswers = 0;
					if(simulationAgent.getSimulationState() == SimulationState.ON){
            			// Send new turn message
						simulationAgent.updateTurn();
						System.out.println("\n\n\n");
						log("Started new turn");
						simulationAgent.sendMessages();
                    }
				}
                customAgent.blockBehaviourIfQueueIsEmpty(this);
			}
		}else {
            customAgent.blockBehaviourIfQueueIsEmpty(this);
    	}
	}
}
