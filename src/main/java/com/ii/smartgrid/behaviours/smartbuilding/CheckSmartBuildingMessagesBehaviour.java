package com.ii.smartgrid.behaviours.smartbuilding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ii.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.model.building.Routine;
import com.ii.smartgrid.model.building.Task;
import com.ii.smartgrid.model.entities.SmartBuilding;
import com.ii.smartgrid.utils.MessageUtil;

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
		// When the owner sends a request with an updated routine this method receives the message 
		if (receivedMsg != null) {
            SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();
			Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            List<Task> tasks = customAgent.readValueFromJson(jsonObject.get(MessageUtil.TASKS), new TypeReference<List<Task>>() {});
            
            Routine routine = smartBuilding.getRoutine();
            
            // Correctly received the new routine, send an "agree"
			customAgent.createAndSendReply(ACLMessage.AGREE, receivedMsg);
            
            boolean result;
            // Update the routine
			if(operation.equals(MessageUtil.ADD)) {
                result = routine.addTasks(tasks, smartBuilding.getAppliances());
            } else if (operation.equals(MessageUtil.REMOVE)){
                result = routine.removeTasks(tasks);
            } else {
                log("Error: invalid parameter \"operation\": " + operation);
                result = false;
            }

            Map<String, Object> content = new HashMap<>();
            content.put(MessageUtil.RESULT, result);
            customAgent.createAndSendReply(ACLMessage.INFORM, receivedMsg, content);

			log("Routine updated");			
		} else {
			block();
		}
	}
	
	
	
}
