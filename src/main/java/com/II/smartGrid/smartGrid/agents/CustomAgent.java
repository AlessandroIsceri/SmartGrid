package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WindSpeedStatus;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public abstract class CustomAgent extends Agent{
	protected int curTurn;
	protected WeatherStatus curWeather;
	protected WindSpeedStatus curWindSpeed;

	public void log(String message){
		//1 - day 1 - 00:15 - HOME1 --> messaggio
		//24:00 = 1440 minutes
		int turnDuration = TimeUtils.getTurnDuration();
		int day = ((curTurn * turnDuration) / 1440) + 1;
		int curDayTurn = curTurn % (1440 / turnDuration);
		System.out.println(this.curTurn + " - " + "Day " + day + " - " + TimeUtils.convertTurnToTime(curDayTurn) + " - " + this.getLocalName() + " --> " + message);
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
        msg.setContent(convertContentToJSON(content));
        return msg;
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
	
}
