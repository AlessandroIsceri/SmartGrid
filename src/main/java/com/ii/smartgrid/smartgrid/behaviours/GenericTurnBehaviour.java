package com.ii.smartgrid.smartgrid.behaviours;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
//import com.II.smartgrid.smartgrid.agents.SendEndTurnMsg;
import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WindSpeedStatus;
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
			Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
			
			int curTurn = (int) jsonObject.get(MessageUtil.CURRENT_TURN);
			((CustomAgent) myAgent).setCurTurn(curTurn);
			int weather = (int) jsonObject.get(MessageUtil.CURRENT_WEATHER);
			((CustomAgent) myAgent).setCurWeather(WeatherStatus.values()[weather]);
			int windSpeed = (int) jsonObject.get(MessageUtil.CURRENT_WIND_SPEED);
			((CustomAgent) myAgent).setCurWindSpeed(WindSpeedStatus.values()[windSpeed]);
					
			SequentialBehaviour sequentialTurnBehaviour = new SequentialBehaviour(myAgent){
             	@Override
             	public int onEnd(){
					((CustomAgent) myAgent).createAndSendReply(ACLMessage.INFORM, receivedMsg);
                    ((CustomAgent) myAgent).log("Turn finished");
                    return 0;
                }
            };
			
			executeTurn(sequentialTurnBehaviour);
            myAgent.addBehaviour(sequentialTurnBehaviour);
            block();
		}else {
			block();
		}
	}

    abstract protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour);
}
