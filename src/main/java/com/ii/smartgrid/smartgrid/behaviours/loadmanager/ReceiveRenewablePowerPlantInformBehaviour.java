package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.LoadManager;
import com.ii.smartgrid.smartgrid.agents.LoadManager;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveRenewablePowerPlantInformBehaviour extends Behaviour{
    
    private int requestCont = 0;
    private boolean finished = false;
    private int renewablePowerPlantCount = ((LoadManager) myAgent).getRenewablePowerPlantNames().size();

    public ReceiveRenewablePowerPlantInformBehaviour(LoadManager loadManager){
        super(loadManager);
    }

    @Override
    public void action() {
        ((LoadManager) myAgent).log("sono stato svegliato (ReceiveRenewablePowerPlantBehaviour)");
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
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
				((LoadManager) myAgent).removeExpectedConsumption(energy);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

            if(requestCont < renewablePowerPlantCount){
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
