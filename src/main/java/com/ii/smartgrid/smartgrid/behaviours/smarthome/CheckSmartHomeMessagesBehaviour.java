package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckSmartHomeMessagesBehaviour extends CyclicBehaviour{

	private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();
	
	public CheckSmartHomeMessagesBehaviour(SmartHomeAgent smartHomeAgent) {
		super(smartHomeAgent);
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
            SmartHome smartHome = ((SmartHomeAgent) myAgent).getSmartHome();
			Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            List<Task> tasks = (List<Task>) ((CustomAgent) myAgent).readValueFromJson(jsonObject.get(MessageUtil.TASKS), new TypeReference<List<Task>>() {});
            
            Routine routine = smartHome.getRoutine();
            
            //rispondo agree; faccio operazioni; dico inform per dire se è andata bene o male
            ((CustomAgent) myAgent).createAndSendReply(ACLMessage.AGREE, receivedMsg);
            
            boolean result = true;
            if(operation.equals(MessageUtil.ADD)) {
                result = routine.addTasks(tasks);
            } else if (operation.equals(MessageUtil.REMOVE)){
                result = routine.removeTasks(tasks);
            } else {
                ((CustomAgent) myAgent).log("Error: invalid parameter \"operation\": " + operation, BEHAVIOUR_NAME);
                result = false;
            }

            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.RESULT, result);
            ((CustomAgent) myAgent).createAndSendReply(ACLMessage.INFORM, receivedMsg, content);

			((CustomAgent) myAgent).log("Routine updated", BEHAVIOUR_NAME);			
		} else {
			block();
		}
	}
	
	
	
}
