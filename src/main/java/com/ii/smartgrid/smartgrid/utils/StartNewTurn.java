package com.ii.smartgrid.smartgrid.utils;

import com.ii.smartgrid.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.smartgrid.utils.SimulationSettingsAgent.SimulationStatus;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class StartNewTurn extends CustomCyclicBehaviour {

	private int receivedAnswers = 0;
    private SimulationSettingsAgent simulationSettingsAgent;
	
	public StartNewTurn(SimulationSettingsAgent simulationSettings) {
		super(simulationSettings);
        this.simulationSettingsAgent = (SimulationSettingsAgent) customAgent;
	}
	
	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName());
			if(receivedMsg.getPerformative() == ACLMessage.INFORM) {
				receivedAnswers++;
                log("Received answers: " + receivedAnswers + " last sender: " + receivedMsg.getSender().getLocalName());
				if(receivedAnswers == simulationSettingsAgent.getAgentNames().size()) {
					receivedAnswers = 0;
					if(simulationSettingsAgent.getSimulationStatus() == SimulationStatus.ON){
            			//send new turn message
						simulationSettingsAgent.updateTurn();
						System.out.println("\n\n\n");
						log("Started new turn");
						simulationSettingsAgent.sendMessages();	
                    }
				}
                customAgent.blockBehaviourIfQueueIsEmpty(this);
			}
		}else {
            customAgent.blockBehaviourIfQueueIsEmpty(this);
    	}
	}
}
