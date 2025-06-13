package com.ii.smartgrid.smartgrid.behaviours;

import java.util.HashMap;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
//import com.II.smartgrid.smartgrid.agents.SendEndTurnMsg;
import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class GenericTurnBehaviour extends CyclicBehaviour{

	private ObjectMapper objectMapper = new ObjectMapper();
	
	public GenericTurnBehaviour(CustomAgent agent) {
        super(agent);
    }

    @Override
	public void action() {
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("turn-" + myAgent.getLocalName()),
												 MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("INIT TURN MSG RECEIVED");
			String receivedContent = receivedMsg.getContent();
			TypeReference<HashMap<String, Integer>> typeRef = new TypeReference<HashMap<String, Integer>>() {};
			HashMap<String, Integer> jsonObject;
			try {
				jsonObject = objectMapper.readValue(receivedContent, typeRef);
				int curTurn = jsonObject.get("curTurn");
				((CustomAgent) myAgent).setCurTurn(curTurn);
				int weather = jsonObject.get("weather");
				((CustomAgent) myAgent).setCurWeatherStatus(WeatherStatus.values()[weather]);
                ((CustomAgent) myAgent).log("Received weather: " + ((CustomAgent) myAgent).getCurWeatherStatus());
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
            ACLMessage replyMsg = receivedMsg.createReply(ACLMessage.INFORM);
			
			SequentialBehaviour sequentialTurnBehaviour = new SequentialBehaviour(myAgent){
             	@Override
             	public int onEnd(){
                    myAgent.send(replyMsg);
                    ((CustomAgent) myAgent).log("Turn finished");
                    return 0;
                }
            };
			
			executeTurn(replyMsg, sequentialTurnBehaviour);
            myAgent.addBehaviour(sequentialTurnBehaviour);
            block();
		}else {
			block();
		}
	}

    abstract protected void executeTurn(ACLMessage replyMessage, SequentialBehaviour sequentialTurnBehaviour);
}
