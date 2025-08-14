package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckSmartHomeMessagesBehaviour extends CustomCyclicBehaviour{
	
    private SmartHomeAgent smartHomeAgent;

	public CheckSmartHomeMessagesBehaviour(SmartHomeAgent smartHomeAgent) {
		super(smartHomeAgent);
        this.smartHomeAgent = smartHomeAgent;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = customAgent.receive(mt);
		//owner -> manda msg request -> smartHome riceve, manda agree ed esegue -> inform è andata bene
		if (receivedMsg != null) {
			/**
			 * {
			 * 		"operation": "add"/"remove"
			 * 		"tasks": task[] sempre JSON 
			 * }
			 */
            SmartHome smartHome = smartHomeAgent.getSmartHome();
			Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            List<Task> tasks = (List<Task>) customAgent.readValueFromJson(jsonObject.get(MessageUtil.TASKS), new TypeReference<List<Task>>() {});
            
            Routine routine = smartHome.getRoutine();
            
            //rispondo agree; faccio operazioni; dico inform per dire se è andata bene o male
            customAgent.createAndSendReply(ACLMessage.AGREE, receivedMsg);
            
            boolean result = true;
            if(operation.equals(MessageUtil.ADD)) {
                result = routine.addTasks(tasks, smartHome.getAppliances());
            } else if (operation.equals(MessageUtil.REMOVE)){
                result = routine.removeTasks(tasks);
            } else {
                log("Error: invalid parameter \"operation\": " + operation);
                result = false;
            }

            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.RESULT, result);
            customAgent.createAndSendReply(ACLMessage.INFORM, receivedMsg, content);

			log("Routine updated");			
		} else {
			block();
		}
	}
	
	
	
}
