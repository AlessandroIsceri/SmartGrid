package com.II.smartGrid.smartGrid.tools;

import java.util.List;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class StartNewTurn extends CyclicBehaviour {

	private int receivedAnswers = 0;
	
	public StartNewTurn(SimulationSettings simulationSettings) {
		super(simulationSettings);
	}
	
	@Override
	public void action() {
		
		ACLMessage receivedMsg = myAgent.receive();
		if (receivedMsg != null) {
			if(receivedMsg.getPerformative() == ACLMessage.INFORM) {
				receivedAnswers++;
				if(receivedAnswers == ((SimulationSettings) myAgent).getAgentNames().size()) {
					receivedAnswers = 0;
					//send new turn message
					((SimulationSettings) myAgent).updateTurn();
					sendMessages();
				}
				block();
			}else if(receivedMsg.getPerformative() == ACLMessage.REQUEST) {
				sendMessages();
			}
		}else {
			block();
		}
	}
	
	private void sendMessages() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> allAgentNames = ((SimulationSettings) myAgent).getAgentNames();
		for(int i = 0; i < allAgentNames.size(); i++) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setConversationId("turn-" + allAgentNames.get(i));
			msg.addReceiver(new AID(allAgentNames.get(i), AID.ISLOCALNAME));
			msg.setContent("{\"curTurn\":" + ((SimulationSettings) myAgent).getCurTurn() 
					      + ", \"turnDuration\": " + ((SimulationSettings) myAgent).getTurnDuration() + "}");
			myAgent.send(msg);
		}
	}

}
