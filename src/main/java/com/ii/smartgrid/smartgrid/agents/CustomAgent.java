package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.CustomObject;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import java.util.Map;
import java.util.function.ObjIntConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Time;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WindSpeedStatus;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public abstract class CustomAgent extends Agent{
	protected int curTurn;
	protected WeatherStatus curWeather;
	protected WindSpeedStatus curWindSpeed;
    protected Logger logger = LoggerFactory.getLogger(CustomAgent.class);
    protected double radiantLatitude;
    protected double radiantLongitude;
    // protected Map<String, Cable> connectedAgents;
    protected CustomObject referencedObject;

    public void blockBehaviourIfQueueIsEmpty(Behaviour b){
        if(getCurQueueSize() == 0){
            b.block();
        }
    }

    protected void log(String message){
        int day = TimeUtils.getCurrentDayFromTurn(curTurn);
		logger.info(this.curTurn + " - " + "Day " + day + " - " + TimeUtils.convertTurnToTime(this.curTurn) + " - " + this.getLocalName() + " --> " + message);
    }

	public void log(String message, String behaviourName){
		int day = TimeUtils.getCurrentDayFromTurn(curTurn);
		logger.info(this.curTurn + " - " + "Day " + day + " - " + TimeUtils.convertTurnToTime(this.curTurn) + " - " + this.getLocalName() + ":" + behaviourName + " --> " + message);
	}
	
	private String convertContentToJSON(Map<String, Object> content){
		ObjectMapper mapper = new ObjectMapper();
        String ret = "";
        try {
            ret = mapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            log("Error: an error occurred while creating the JSON content");
            e.printStackTrace();
        }
        return ret;
	}

    public void createAndSendReply(int performative, ACLMessage msgReceived){
        ACLMessage reply = msgReceived.createReply(performative);
        this.send(reply);
    }

	public void createAndSendReply(int performative, ACLMessage msgReceived, Map<String, Object> content){
        ACLMessage reply = msgReceived.createReply(performative);
        reply.setContent(convertContentToJSON(content));
        this.send(reply);
    }

    public void createAndSend(int performative, String receiverName, Map<String, Object> content){
		this.send(buildMessage(performative, receiverName, content));
	}


	public void createAndSend(int performative, String receiverName, Map<String, Object> content, String conversationId){
		ACLMessage msg = buildMessage(performative, receiverName, content);
        msg.setConversationId(conversationId);
        this.send(msg);
	}


	private ACLMessage buildMessage(int performative, String receiverName, Map<String, Object> content){
		ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));

        Object givenEnergy = content.get(MessageUtil.GIVEN_ENERGY);
        if(givenEnergy != null){
            content.put(MessageUtil.GIVEN_ENERGY, updateEnergyValue(receiverName, (double) givenEnergy));
        }

        Object releasedEnergy = content.get(MessageUtil.GIVEN_ENERGY);
        if(releasedEnergy != null){
            content.put(MessageUtil.RELEASED_ENERGY, updateEnergyValue(receiverName, (double) releasedEnergy));
        }

        msg.setContent(convertContentToJSON(content));
        return msg;
	}

    private double updateEnergyValue(String receiverName, double producedEnergy){
        Map<String, Cable> connectedAgents = referencedObject.getConnectedAgents();
        Cable cable = connectedAgents.get(receiverName);
        return cable.computeTransmittedPower(producedEnergy);
    }

    public Map<String, Object> convertAndReturnContent(ACLMessage receivedMessage){
        String receivedContent = receivedMessage.getContent();
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> jsonObject = null;
		try {
            jsonObject = objectMapper.readValue(receivedContent, typeRef);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
		return jsonObject;
    }

	public int getCurTurn() {
		return curTurn;
	}
	
	public void setCurTurn(int curTurn) {
		this.curTurn = curTurn;
	}

	public WeatherStatus getCurWeather() {
		return curWeather;
	}

	public void setCurWeather(WeatherStatus curWeatherStatus) {
		this.curWeather = curWeatherStatus;
	}

    public WindSpeedStatus getCurWindSpeed() {
        return curWindSpeed;
    }

    public void setCurWindSpeed(WindSpeedStatus curWindSpeed) {
        this.curWindSpeed = curWindSpeed;
    }

    public CustomObject getReferencedObject() {
        return referencedObject;
    }

}
