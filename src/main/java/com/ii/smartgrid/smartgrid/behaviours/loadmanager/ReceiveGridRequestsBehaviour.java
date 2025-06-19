package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.LoadManager;
import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.agents.LoadManager;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveGridRequestsBehaviour extends Behaviour{
    
    private boolean finished = false;
    private int requestCont = 0;
    private int gridCount = ((LoadManager) myAgent).getGridNames().size();

    public ReceiveGridRequestsBehaviour (LoadManager loadManager){
        super(loadManager);
    }

    @Override
    public void action() {
        ((LoadManager) myAgent).log("sono stato svegliato (ReceiveGridRequestsBehaviour)");
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((LoadManager) myAgent).log("ho ricevuto una richiesta");
            requestCont++;
			String receivedContent = receivedMsg.getContent();
			/**
			 * {
			 * 		"energy": 200.0
			 * }
			 */
            ObjectMapper objectMapper = new ObjectMapper();
			try {
				TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
				HashMap<String, Object> jsonObject;
				jsonObject = objectMapper.readValue(receivedContent, typeRef);
				double energy = (double) jsonObject.get("energy");
                ((LoadManager) myAgent).log("energy: " + energy);
                String sender = receivedMsg.getSender().getLocalName();
                ((LoadManager) myAgent).addGridRequestedEnergy(sender, energy);
				((LoadManager) myAgent).addExpectedConsumption(energy);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

            if(requestCont < gridCount){
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
