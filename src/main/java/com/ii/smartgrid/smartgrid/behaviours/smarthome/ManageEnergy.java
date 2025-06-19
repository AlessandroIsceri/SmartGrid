package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyProducer;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.sniffer.Message;

public class ManageEnergy extends Behaviour{
	
    private enum Status {SETUP, RECEIVE_ANSWER, FINISHED}
    private Status state = Status.SETUP;

	public ManageEnergy(SmartHome smartHome) {
		super(smartHome);
	}

	@Override
	public void action() {
        switch(state){
            case SETUP:
                setup();
                break;
            case RECEIVE_ANSWER:
                receive_answer();
                break;
            default:
                break;
        }
	}

    private void setup(){
        ((SmartHome) myAgent).log("SETUP STARTED");
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //può rilasciare energia se ne ha troppa e non gli serve
		double expectedConsumption = ((SmartHome) myAgent).getExpectedConsumption();
		
		List<EnergyProducer> energyProducers = ((SmartHome) myAgent).getEnergyProducers();
		
		int hour = TimeUtils.getHourFromTurn(((SmartHome) myAgent).getCurTurn());
		double expectedProduction = 0;
		WeatherStatus curWeatherStatus = ((SmartHome) myAgent).getCurWeather();
		for(int i = 0; i < energyProducers.size(); i++) {
            //hproduction / 60 * turnDuration)
			expectedProduction += energyProducers.get(i).getHProduction(curWeatherStatus, hour) / 60 * TimeUtils.getTurnDuration();
		}
        ((SmartHome) myAgent).log("PV: expectedProduction: " + expectedProduction);
		
		Battery battery = ((SmartHome) myAgent).getBattery();
		double availableEnergy = expectedProduction;

		if(battery != null){
			availableEnergy += battery.getStoredEnergy();
		}

        if(availableEnergy > expectedConsumption) {
			// Store extra energy into home's battery (if available)
            String gridName = ((SmartHome) myAgent).getGridName();
            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.OPERATION, MessageUtil.RELEASE);
			if(battery != null){
				double extraEnergy = battery.fillBattery(availableEnergy - expectedConsumption);
                // Release extra energy into the grid
                content.put(MessageUtil.RELEASED_ENERGY, extraEnergy);
			} else {
				// Battery not available -> release extra energy into the grid
                content.put(MessageUtil.RELEASED_ENERGY, (availableEnergy - expectedConsumption));
			}
            ((SmartHome) myAgent).createAndSend(ACLMessage.INFORM, gridName, content, "release-" + myAgent.getLocalName());
            state = Status.FINISHED;
		} else {
			// Request energy from the grid
            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.OPERATION, MessageUtil.CONSUME);
            content.put(MessageUtil.REQUESTED_ENERGY, (expectedConsumption - availableEnergy));
            ((SmartHome) myAgent).createAndSend(ACLMessage.REQUEST, ((SmartHome) myAgent).getGridName(), content);
            state = Status.RECEIVE_ANSWER;
		}
        ((SmartHome) myAgent).log("ManageEnergy FINISHED");
        block();
    }

    private void receive_answer(){
        MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), 
                                                MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        ACLMessage receivedMsg = myAgent.receive();
		if (receivedMsg != null) {
            ((SmartHome) myAgent).log("ANSWER RECEIVED");
            String receivedContent = receivedMsg.getContent();
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
            HashMap<String, Object> jsonObject;
            ObjectMapper objectMapper = new ObjectMapper();
            if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                try {
                    jsonObject = objectMapper.readValue(receivedContent, typeRef);
                    String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                    double energy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
                    if(operation.equals(MessageUtil.CONSUME)){
                        ((SmartHome) myAgent).log("Energy consumed");
                    }else{
                        ((SmartHome) myAgent).log("Error: invalid operation");
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }   
            } else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                //possible blackout 
                try {
                    jsonObject = objectMapper.readValue(receivedContent, typeRef);
                    String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                    double energy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
                    if(operation.equals(MessageUtil.CONSUME)){
                        ((SmartHome) myAgent).log("ATTENTION: Blackout soon");
                        ((SmartHome) myAgent).shutDown();
                    }else{
                        ((SmartHome) myAgent).log("Error: invalid operation");
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
		} else {
			block();
		}
        state = Status.FINISHED;
    }

    @Override
    public boolean done() {
        return state == Status.FINISHED;
    }

}
