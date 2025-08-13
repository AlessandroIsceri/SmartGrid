package com.ii.smartgrid.smartgrid.utils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.SimulationStatus;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class StartNewTurn extends CustomCyclicBehaviour {

	private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

	private int receivedAnswers = 0;
    private SimulationSettings myAgent;
	
	public StartNewTurn(SimulationSettings simulationSettings) {
		super(simulationSettings);
		// myAgent = ((SimulationSettings) myAgent);
		myAgent = ((SimulationSettings) super.myAgent);
	}
	
	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            myAgent.log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);
			if(receivedMsg.getPerformative() == ACLMessage.INFORM) {
				receivedAnswers++;
                ((SimulationSettings) myAgent).log("Received answers: " + receivedAnswers + " last sender: " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);
				if(receivedAnswers == ((SimulationSettings) myAgent).getAgentNames().size()) {
					receivedAnswers = 0;
					if(((SimulationSettings) myAgent).getSimulationStatus() == SimulationStatus.ON){
            			//send new turn message
						((SimulationSettings) myAgent).updateTurn();
						System.out.println("\n\n\n");
						((SimulationSettings) myAgent).log("Started new turn", BEHAVIOUR_NAME);
						((SimulationSettings) myAgent).sendMessages();	
                    }
				}
                ((SimulationSettings) myAgent).blockBehaviourIfQueueIsEmpty(this);
			}
		}else {
            ((SimulationSettings) myAgent).blockBehaviourIfQueueIsEmpty(this);
    	}
	}
}
