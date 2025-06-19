package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.agents.SmartHome;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ManageEnergyRequestBehaviour extends Behaviour{

    private int requestCont = 0;
    private boolean finished = false;
    private ObjectMapper objectMapper = new ObjectMapper();

    public ManageEnergyRequestBehaviour(Grid grid){
        super(grid);
    }

    @Override
    public void action() {
        ((Grid) myAgent).log("sono stato svegliato (ManageEnergyRequestBehaviour)");
		//request from non black out, inform from black out homes
		MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), 
                                                MessageTemplate.MatchPerformative(ACLMessage.INFORM));		
        ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((Grid) myAgent).log("ho ricevuto una richiesta");
            requestCont++;
            String receivedContent = receivedMsg.getContent();
            String sender = receivedMsg.getSender().getLocalName();
            if(receivedMsg.getPerformative() == ACLMessage.REQUEST){
				/**
				 * {
				 * 		"operation": "release"/"consume"
				 * 		"energy": 200.0
				 * }
				 */
				try {
					TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
					HashMap<String, Object> jsonObject;
					jsonObject = objectMapper.readValue(receivedContent, typeRef);
					String operation = (String) jsonObject.get("operation");
					double energy = (double) jsonObject.get("energy");
					
					((Grid) myAgent).log("operation: " + operation);
					((Grid) myAgent).log("energy: " + energy);
					
					if (operation.equals("consume")) {
						((Grid) myAgent).addExpectedConsumption(energy);
                        ((Grid) myAgent).addEnergyRequest(sender, energy);
					} else {
						((Grid) myAgent).log("Error: invalid parameter \"operation\": " + operation);
					}
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
            }else if(receivedMsg.getPerformative() == ACLMessage.INFORM){
                //{"blackout": true/false}
				// false -> energy restored independently
				// true -> home still in blackout
                TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
                HashMap<String, Object> jsonObject;
                String conversationId = receivedMsg.getConversationId();
                try {
                    jsonObject = objectMapper.readValue(receivedContent, typeRef);
                    String operation = (String) jsonObject.get("operation");
                    if(conversationId.contains("blackout")){
                        boolean blackout = (boolean) jsonObject.get("blackout");
                        if(!blackout){
                            ((Grid) myAgent).removeSmartHomeWithoutPower(sender);
                        }
                    } else{
                        double energy = (double) jsonObject.get("energy");
                        ((Grid) myAgent).removeExpectedConsumption(energy);
                        if(((Grid) myAgent).containsSmartHomeWithoutPower(sender)){
                            // false -> energy restored independently
                            // true -> home still in blackout		
                            boolean blackout = (boolean) jsonObject.get("blackout");
                            if(!blackout){
                                ((Grid) myAgent).removeSmartHomeWithoutPower(sender);
                            }
                        }
                    }
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            int smartHomesCount = ((Grid) myAgent).getSmartHomeNames().size();
            if(requestCont < smartHomesCount){
                block();
            }else{
                finished = true;
            }
		} else {
			block();
		}
    }

    @Override
    public boolean done() {
        return finished;
    }
}
