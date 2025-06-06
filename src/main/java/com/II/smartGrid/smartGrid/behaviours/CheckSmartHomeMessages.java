package com.II.smartGrid.smartGrid.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.II.smartGrid.smartGrid.agents.SmartHome;
import com.II.smartGrid.smartGrid.model.Routine;
import com.II.smartGrid.smartGrid.model.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class CheckSmartHomeMessages extends CyclicBehaviour{
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public CheckSmartHomeMessages(SmartHome smartHome) {
		super(smartHome);
	}

	@Override
	public void action() {
		ACLMessage receivedMsg = myAgent.receive();
		//owner -> manda msg request -> smartHome riceve, manda agree ed esegue -> inform è andata bene
		if (receivedMsg != null) {
			if(receivedMsg.getPerformative() == ACLMessage.REQUEST) {
				String receivedContent = receivedMsg.getContent();
				/**
				 * {
				 * 		"operation": "add"/"remove"
				 * 		"tasks": task[] sempre JSON 
				 * }
				 */
				TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
				HashMap<String, Object> jsonObject;
				
				try {
					jsonObject = objectMapper.readValue(receivedContent, typeRef);
					String operation = (String) jsonObject.get("operation");
					ArrayList<Task> tasks = (ArrayList<Task>) objectMapper.convertValue(jsonObject.get("tasks"), new TypeReference<List<Task>>() {});
					Routine routine = ((SmartHome) myAgent).getRoutine();
					
					//rispondo agree; faccio operazioni; dico inform per dire se è andata bene o male
					ACLMessage replyMsg = receivedMsg.createReply(ACLMessage.AGREE);
					myAgent.send(replyMsg);
					
					boolean result = true;
					if(operation.equals("add")) {
						result = routine.addTasks(tasks);
					} else if (operation.equals("remove")){
						 result = routine.removeTasks(tasks);
					} else {
						System.out.println("Error: invalid parameter \"operation\": " + operation);
						result = false;
					}
					System.out.println("NEW ROUTINE FOR " + myAgent.getName() + " " + ((SmartHome) myAgent).getRoutine().toString());
					ACLMessage replyMsgInform = receivedMsg.createReply(ACLMessage.INFORM);
					replyMsgInform.setContent("{\"result\": " + result + "}");
					myAgent.send(replyMsgInform);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
		} else {
			block();
		}
	}
	
	
	
}
