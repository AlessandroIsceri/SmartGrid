package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DistributeEnergy extends CyclicBehaviour{

	private ObjectMapper objectMapper = new ObjectMapper();

	public DistributeEnergy(Grid grid) {
		super(grid);
	}
	
	@Override
	public void action() {
        ((CustomAgent) myAgent).log("sono stato svegliato (DistributeEnergy)");
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("ho ricevuto una richiesta");
			String receivedContent = receivedMsg.getContent();
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
                ((CustomAgent) myAgent).log("operation: " + operation);
                ((CustomAgent) myAgent).log("energy: " + energy);
				
				if(operation.equals("release")) {
                    String sender = receivedMsg.getSender().getLocalName();
                    if(((Grid) myAgent).containsSmartHomeWithoutPower(sender)){
                        ((Grid) myAgent).removeSmartHomeWithoutPower(sender);
                    }

                    ACLMessage replyMsg = receivedMsg.createReply(ACLMessage.AGREE);
                    replyMsg.setContent(receivedContent);
                    myAgent.send(replyMsg);
                    //excess energy will be lost
                    double excess = ((Grid) myAgent).addEnergy(energy);
				} else if (operation.equals("consume")){
                    if (((Grid) myAgent).consumeEnergy(energy)) {
                        ACLMessage replyMsg = receivedMsg.createReply(ACLMessage.AGREE);
                        replyMsg.setContent(receivedContent);
                        myAgent.send(replyMsg);
                    }else{
                        ACLMessage replyMsg = receivedMsg.createReply(ACLMessage.REFUSE);
                        replyMsg.setContent(receivedContent);
                        myAgent.send(replyMsg);
                    }
				} else {
					((SmartHome) myAgent).log("Error: invalid parameter \"operation\": " + operation);
				}
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} else {
			block();
		}
	}
	
}
