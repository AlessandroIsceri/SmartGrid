package com.ii.smartgrid.smartgrid.behaviours.smartbuilding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.SmartBuilding;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckSmartBuildingMessagesBehaviour extends CustomCyclicBehaviour{
	
    private SmartBuildingAgent smartBuildingAgent;

	public CheckSmartBuildingMessagesBehaviour(SmartBuildingAgent smartBuildingAgent) {
		super(smartBuildingAgent);
        this.smartBuildingAgent = smartBuildingAgent;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = customAgent.receive(mt);
		//owner -> manda msg request -> smartBuilding riceve, manda agree ed esegue -> inform è andata bene
		if (receivedMsg != null) {
			/**
			 * {
			 * 		"operation": "add"/"remove"
			 * 		"tasks": task[] sempre JSON 
			 * }
			 */
            SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();
			Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            List<Task> tasks = (List<Task>) customAgent.readValueFromJson(jsonObject.get(MessageUtil.TASKS), new TypeReference<List<Task>>() {});
            
            Routine routine = smartBuilding.getRoutine();
            
            //rispondo agree; faccio operazioni; dico inform per dire se è andata bene o male
            customAgent.createAndSendReply(ACLMessage.AGREE, receivedMsg);
            
            boolean result = true;
            if(operation.equals(MessageUtil.ADD)) {
                result = routine.addTasks(tasks, smartBuilding.getAppliances());
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
