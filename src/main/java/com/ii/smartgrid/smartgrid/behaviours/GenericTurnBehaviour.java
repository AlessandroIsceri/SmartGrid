package com.ii.smartgrid.smartgrid.behaviours;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WindSpeedStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class GenericTurnBehaviour extends CyclicBehaviour{

	private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

	
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
            double electricityPrice = (double) jsonObject.get(MessageUtil.ELECTRICITY_PRICE);
			((CustomAgent) myAgent).setCurElectricityPrice(electricityPrice);
					
			SequentialBehaviour sequentialTurnBehaviour = new SequentialBehaviour(myAgent){
             	@Override
             	public int onEnd(){
					((CustomAgent) myAgent).createAndSendReply(ACLMessage.INFORM, receivedMsg);
                    ((CustomAgent) myAgent).log("Turn finished", BEHAVIOUR_NAME);
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
