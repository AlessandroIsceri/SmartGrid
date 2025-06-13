package com.ii.smartgrid.smartgrid.utils;

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
					System.out.println("\n");
					((SimulationSettings) myAgent).log("Started new turn");
				}
				block();
			}else if(receivedMsg.getPerformative() == ACLMessage.REQUEST) {
				sendMessages();
				((SimulationSettings) myAgent).log("Started first turn");
			}
		}else {
			block();
		}
	}
	
	private void sendMessages() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String content = "{\"curTurn\":" + ((SimulationSettings) myAgent).getCurTurn()
					      + ", \"weather\": "+ ((SimulationSettings) myAgent).getCurWeatherStatus().ordinal() + "}";
		List<String> allAgentNames = ((SimulationSettings) myAgent).getAgentNames();
		for(int i = 0; i < allAgentNames.size(); i++) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setConversationId("turn-" + allAgentNames.get(i));
			msg.addReceiver(new AID(allAgentNames.get(i), AID.ISLOCALNAME));
			msg.setContent(content);
			myAgent.send(msg);
		}
	}

}
