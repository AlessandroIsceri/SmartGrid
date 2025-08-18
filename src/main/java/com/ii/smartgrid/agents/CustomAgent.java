package com.ii.smartgrid.agents;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.model.entities.CustomObject;
import com.ii.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.ii.smartgrid.utils.WeatherUtil.WindSpeedStatus;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

// Abstract agent class that will be extended by all other agents
// It contains generic methods for creating and sending messages, logging information, and common information shared by all agents
public abstract class CustomAgent extends Agent{

	protected int curTurn;
	protected WeatherStatus curWeather;
	protected WindSpeedStatus curWindSpeed;
    protected Logger logger = LoggerFactory.getLogger(CustomAgent.class);
    protected CustomObject referencedObject;
    protected double curElectricityPrice;
    private ObjectMapper objectMapper;

    protected CustomAgent(){
        super();
        objectMapper = new ObjectMapper();
    }

    public void blockBehaviourIfQueueIsEmpty(Behaviour b){
        if(getCurQueueSize() == 0){
            b.block();
        }
    }

    protected void log(String message){
        int day = TimeUtils.getCurrentDayFromTurn(curTurn);
        logger.info("{} - Day {} - {} - {} --> {}", this.curTurn, day, TimeUtils.convertTurnToTime(this.curTurn), this.getLocalName(), message);
    }

	public void log(String message, String behaviourName){
		int day = TimeUtils.getCurrentDayFromTurn(curTurn);
        logger.info("{} - Day {} - {} - {}:{} --> {}", this.curTurn, day, TimeUtils.convertTurnToTime(this.curTurn), this.getLocalName(), behaviourName, message);
	}
	
	private String convertContentToJSON(Map<String, Object> content){
        String ret = "";
        try {
            ret = this.objectMapper.writeValueAsString(content);
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
        msg.setContent(convertContentToJSON(content));
        return msg;
	}

    public Map<String, Object> convertAndReturnContent(ACLMessage receivedMessage){
        String receivedContent = receivedMessage.getContent();
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};

		Map<String, Object> jsonObject = null;
		try {
            jsonObject = this.objectMapper.readValue(receivedContent, typeRef);
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

    public double getCurElectricityPrice() {
        return curElectricityPrice;
    }

    public void setCurElectricityPrice(double curElectricityPrice) {
        this.curElectricityPrice = curElectricityPrice;
    }

    public <T> T readValueFromJson(Object object, Class<T> clazz) {
        return this.objectMapper.convertValue(object, clazz);
    }

    public <T> T readValueFromJson(Object object, TypeReference<T> typeReference) {
        return this.objectMapper.convertValue(object, typeReference);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    

}
