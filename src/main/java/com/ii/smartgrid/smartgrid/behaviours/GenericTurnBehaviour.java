package com.ii.smartgrid.smartgrid.behaviours;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WindSpeedStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class GenericTurnBehaviour extends CustomCyclicBehaviour{

	
	public GenericTurnBehaviour(CustomAgent agent) {
        super(agent);
    }

    @Override
	public void action() {
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("turn-" + customAgent.getLocalName()),
												 MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
			Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
			
			int curTurn = (int) jsonObject.get(MessageUtil.CURRENT_TURN);
			customAgent.setCurTurn(curTurn);
			int weather = (int) jsonObject.get(MessageUtil.CURRENT_WEATHER);
			customAgent.setCurWeather(WeatherStatus.values()[weather]);
			int windSpeed = (int) jsonObject.get(MessageUtil.CURRENT_WIND_SPEED);
			customAgent.setCurWindSpeed(WindSpeedStatus.values()[windSpeed]);
            double electricityPrice = (double) jsonObject.get(MessageUtil.ELECTRICITY_PRICE);
			customAgent.setCurElectricityPrice(electricityPrice);
					
			SequentialBehaviour sequentialTurnBehaviour = new SequentialBehaviour(customAgent){
             	@Override
             	public int onEnd(){
					customAgent.createAndSendReply(ACLMessage.INFORM, receivedMsg);
                    return 0;
                }
            };
			
			executeTurn(sequentialTurnBehaviour);
            customAgent.addBehaviour(sequentialTurnBehaviour);
            block();
		}else {
			block();
		}
	}

    abstract protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour);
}
