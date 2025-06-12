package com.II.smartGrid.smartGrid.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.II.smartGrid.smartGrid.agents.Grid;
import com.II.smartGrid.smartGrid.agents.SmartHome;
import com.II.smartGrid.smartGrid.model.Routine;
import com.II.smartGrid.smartGrid.model.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
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
				
				if(operation.equals("release")) {
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
