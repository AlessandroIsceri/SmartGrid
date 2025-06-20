package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckSmartHomeMessagesBehaviour extends CyclicBehaviour{
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public CheckSmartHomeMessagesBehaviour(SmartHome smartHome) {
		super(smartHome);
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = myAgent.receive(mt);
		//owner -> manda msg request -> smartHome riceve, manda agree ed esegue -> inform è andata bene
		if (receivedMsg != null) {
			/**
			 * {
			 * 		"operation": "add"/"remove"
			 * 		"tasks": task[] sempre JSON 
			 * }
			 */
			Map<String, Object> jsonObject = ((SmartHome) myAgent).convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            ArrayList<Task> tasks = (ArrayList<Task>) objectMapper.convertValue(jsonObject.get(MessageUtil.TASKS), new TypeReference<List<Task>>() {});
            Routine routine = ((SmartHome) myAgent).getRoutine();
            
            //rispondo agree; faccio operazioni; dico inform per dire se è andata bene o male
            
            ((SmartHome) myAgent).createAndSendReply(ACLMessage.AGREE, receivedMsg);
            
            boolean result = true;
            if(operation.equals(MessageUtil.ADD)) {
                result = routine.addTasks(tasks);
            } else if (operation.equals(MessageUtil.REMOVE)){
                    result = routine.removeTasks(tasks);
            } else {
                ((SmartHome) myAgent).log("Error: invalid parameter \"operation\": " + operation);
                result = false;
            }

            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.RESULT, result);
            ((SmartHome) myAgent).createAndSendReply(ACLMessage.INFORM, receivedMsg, content);

			((SmartHome) myAgent).log("Routine updated");
			
		} else {
			block();
		}
	}
	
	
	
}
