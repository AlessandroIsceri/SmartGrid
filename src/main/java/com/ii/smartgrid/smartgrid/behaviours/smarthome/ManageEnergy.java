package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.HashMap;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyProducer;
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
		
		//0:15 * 10 = 150 / 60 = 2
		int hour = (TimeUtils.getTurnDuration() * ((SmartHome) myAgent).getCurTurn()) / 60;
		double expectedProduction = 0;
		WeatherStatus curWeatherStatus = ((SmartHome) myAgent).getCurWeatherStatus();
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
			if(battery != null){
				double extraEnergy = battery.fillBattery(expectedConsumption - availableEnergy);
				if(extraEnergy > 0) {
					// Release extra energy into the grid
					sendReleaseEnergyMsg(extraEnergy);
				} else {
                    state = Status.FINISHED;
                }
			} else {
				// Battery not available -> release extra energy into the grid
				sendReleaseEnergyMsg(expectedConsumption - availableEnergy);
			}
		} else {
			// Request energy from the grid
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID(((SmartHome) myAgent).getGridName(), AID.ISLOCALNAME));
            msg.setContent("{ \"operation\" : \"consume\", \"energy\": " + (expectedConsumption - availableEnergy) + "}");
            myAgent.send(msg);
            ((SmartHome) myAgent).log("MESSAGE info:  " + msg.toString());
            state = Status.RECEIVE_ANSWER;
		}
        ((SmartHome) myAgent).log("ManageEnergy FINISHED");
        block();
    }
	
	private void sendReleaseEnergyMsg(double energy) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(new AID(((SmartHome) myAgent).getGridName(), AID.ISLOCALNAME));
		msg.setContent("{ \"operation\" : \"release\", \"energy\": " + energy + "}");
		myAgent.send(msg);
        ((SmartHome) myAgent).log("MESSAGE info:  " + msg.toString());
        state = Status.RECEIVE_ANSWER;
	}


    private void receive_answer(){
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
                    String operation = (String) jsonObject.get("operation");
                    double energy = (double) jsonObject.get("energy");
                    if(operation.equals("consume")){
                        ((SmartHome) myAgent).log("Energy consumed");
                    }else if(operation.equals("release")){
                        ((SmartHome) myAgent).log("Energy released");
                    }else{
                        ((SmartHome) myAgent).log("Error: invalid operation");
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }   
            } else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                //TODO: VAI IN BLACKOUT;
                try {
                    jsonObject = objectMapper.readValue(receivedContent, typeRef);
                    String operation = (String) jsonObject.get("operation");
                    double energy = (double) jsonObject.get("energy");
                    if(operation.equals("consume")){
                        ((SmartHome) myAgent).log("ATTENTION: Blackout soon");
                        ACLMessage replyMsg = receivedMsg.createReply(ACLMessage.INFORM);
                        replyMsg.setContent("{\"blackout\": true}");
                        myAgent.send(replyMsg);
                        ((SmartHome) myAgent).shutDown();
                    }else if(operation.equals("release")){
                        ((SmartHome) myAgent).log("Energy lost (grid and batteries (if present) are full)");
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
