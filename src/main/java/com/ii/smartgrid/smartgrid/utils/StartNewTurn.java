package com.ii.smartgrid.smartgrid.utils;

import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class StartNewTurn extends CyclicBehaviour {

	private int receivedAnswers = 0;
	
	public StartNewTurn(SimulationSettings simulationSettings) {
		super(simulationSettings);
	}
	
	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
                                                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
			if(receivedMsg.getPerformative() == ACLMessage.INFORM) {
				receivedAnswers++;
                ((SimulationSettings) myAgent).log("STARTED StartNewTurn Behaviour, received answers: " + receivedAnswers);
				if(receivedAnswers == ((SimulationSettings) myAgent).getAgentNames().size()) {
					receivedAnswers = 0;
					//send new turn message
					((SimulationSettings) myAgent).updateTurn();
                    System.out.println("\n\n\n");
					((SimulationSettings) myAgent).log("Started new turn");
					sendMessages();	
				}
				block();
			}else if(receivedMsg.getPerformative() == ACLMessage.REQUEST) {
				sendMessages();
				((SimulationSettings) myAgent).log("Started first turn");
                block();
			}
		}else {
			block();
		}
	}
	
	private void sendMessages() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String content = "{\"curTurn\":" + ((SimulationSettings) myAgent).getCurTurn()
					      + ", \"weather\": "+ ((SimulationSettings) myAgent).getCurWeather().ordinal() 
                          + ", \"windSpeed\": "+ ((SimulationSettings) myAgent).getCurWindSpeed().ordinal() + "}";

        ((CustomAgent) myAgent).log("Weather: " + ((CustomAgent) myAgent).getCurWeather());
        ((CustomAgent) myAgent).log("Wind speed: " + ((CustomAgent) myAgent).getCurWindSpeed());

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
