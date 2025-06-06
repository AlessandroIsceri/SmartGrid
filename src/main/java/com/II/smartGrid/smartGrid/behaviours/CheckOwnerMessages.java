package com.II.smartGrid.smartGrid.behaviours;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.II.smartGrid.smartGrid.agents.Owner;
import com.II.smartGrid.smartGrid.agents.SmartHome;
import com.II.smartGrid.smartGrid.model.Appliance;
import com.II.smartGrid.smartGrid.model.Routine;
import com.II.smartGrid.smartGrid.model.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class CheckOwnerMessages extends CyclicBehaviour {
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public CheckOwnerMessages(Owner owner) {
		super(owner);
	}
	
	@Override
	public void action() {
		ACLMessage receivedMsg = myAgent.receive();
		if (receivedMsg != null) {
			if (receivedMsg.getPerformative() == ACLMessage.REQUEST) {
				/**
				 * {
				 * 		"operation": "add"/"remove"
				 * 		"smartHome": smartHomeAID.getName()
				 * 		"tasks": task[] sempre JSON 
				 * }
				 */
				TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
				HashMap<String, Object> jsonObject;
				
				String receivedContent = receivedMsg.getContent();
				try {
					jsonObject = objectMapper.readValue(receivedContent, typeRef);
					String operation = (String) jsonObject.get("operation");
					String homeID = (String) jsonObject.get("smartHome");
					ArrayList<Task> tasks = (ArrayList<Task>) objectMapper.convertValue(jsonObject.get("tasks"), new TypeReference<List<Task>>() {});
					
					ACLMessage replyMsg = receivedMsg.createReply(ACLMessage.AGREE);
					myAgent.send(replyMsg);
					
					List<String> smartHomesNames = ((Owner) myAgent).getSmartHomesNames();
					for(int i = 0; i < smartHomesNames.size(); i++) {
						String curName = smartHomesNames.get(i);
						if(curName.equals(homeID)) {
							ACLMessage updateRoutineMsg = new ACLMessage(ACLMessage.REQUEST);
							jsonObject.remove("smartHome");
							updateRoutineMsg.setContent(objectMapper.writeValueAsString(jsonObject));
							updateRoutineMsg.setConversationId(receivedMsg.getConversationId());
							AID smartHomeAID = new AID(curName, AID.ISLOCALNAME);
							updateRoutineMsg.addReceiver(smartHomeAID);
							myAgent.send(updateRoutineMsg);
						}
					}
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}else if(receivedMsg.getPerformative() == ACLMessage.AGREE){
				//owner -> manda msg request -> smartHome riceve, manda agree ed esegue -> inform è andata bene
				System.out.println("RECEIVED AGREE FOR " + receivedMsg.getConversationId());
			}else if(receivedMsg.getPerformative() == ACLMessage.INFORM){
				//msg fatto da noi avrà un conversationId -> che viene mantenuto tra i msg
				System.out.println("INFORM RECEIVED FOR " + receivedMsg.getConversationId());
			}
		}else {
			block();
		}
	}
}
