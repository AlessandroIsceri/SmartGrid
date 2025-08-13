package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.CustomObject;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
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
    // protected Map<String, Cable> connectedAgents;
    protected CustomObject referencedObject;
    protected double curElectricityPrice;
    private ObjectMapper objectMapper;

    protected CustomAgent(){
        super();
        objectMapper = new ObjectMapper();
        // BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        //                                                                  .allowIfSubType("com.ii.smartgrid.smartgrid.model")
        //                                                                  .allowIfSubType("java.util")
        //                                                                  .allowIfSubType("java.lang")
        //                                                                  .build();

        // objectMapper.activateDefaultTyping(ptv, DefaultTyping.OBJECT_AND_NON_CONCRETE);
        //  objectMapper.activateDefaultTyping(ptv, DefaultTyping.NON_CONCRETE_AND_ARRAYS);
    }

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
        String ret = "";
        try {
            //TODO RIMUOVI
            content.put(MessageUtil.CURRENT_TURN, curTurn);
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

    public double updateEnergyValue(String receiverName, double producedEnergy){
        Map<String, Cable> connectedAgents = referencedObject.getConnectedAgents();
        Cable cable = connectedAgents.get(receiverName);
        return cable.computeTransmittedPower(producedEnergy);
    }

    public Map<String, Object> convertAndReturnContent(ACLMessage receivedMessage){
        String receivedContent = receivedMessage.getContent();
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};

		Map<String, Object> jsonObject = null;
		try {
            jsonObject = this.objectMapper.readValue(receivedContent, typeRef);
            //TODO RIMUOVI (e rimuovi anche cur turn come content)
            // int curTurnFromMsg = (int) jsonObject.get(MessageUtil.CURRENT_TURN);
            // if(curTurnFromMsg != curTurn){
            //     log("WRONG TURN " + curTurnFromMsg);
            //     log("sender " + receivedMessage.getSender().getLocalName());
            //     log("content " + receivedContent);
            // }
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
        String jsonString;
        // try {
        //     jsonString = objectMapper.writeValueAsString(object);
        //     log("***" + jsonString);
        //     return objectMapper.readValue(jsonString, typeReference);
        // } catch (JsonProcessingException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
        return this.objectMapper.convertValue(object, typeReference);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    

}
