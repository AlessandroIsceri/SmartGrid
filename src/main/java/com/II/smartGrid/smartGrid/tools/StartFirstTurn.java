package com.II.smartGrid.smartGrid.tools;

import java.util.List;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class StartFirstTurn extends OneShotBehaviour {

	public StartFirstTurn(SimulationSettings simulationSettings) {
		super(simulationSettings);
	}
	
	@Override
	public void action() {
		List<String> allAgentNames = ((SimulationSettings) myAgent).getAgentNames();
		for(int i = 0; i < allAgentNames.size(); i++) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setConversationId("turn" + allAgentNames.get(i));
			msg.addReceiver(new AID(allAgentNames.get(i), AID.ISLOCALNAME));
			msg.setContent("{\"curTurn\":" + ((SimulationSettings) myAgent).getCurTurn() 
					      + ", \"turnDuration\": " + ((SimulationSettings) myAgent).getTurnDuration() + "}");
			myAgent.send(msg);
		}
	}

}
