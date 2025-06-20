package com.ii.smartgrid.smartgrid.behaviours.owner;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.Owner;
import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class CheckOwnerMessagesBehaviour extends CyclicBehaviour {
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public CheckOwnerMessagesBehaviour(Owner owner) {
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
				Map<String, Object> jsonObject = ((Owner) myAgent).convertAndReturnContent(receivedMsg);
				String homeName = (String) jsonObject.get(MessageUtil.SMART_HOME);
                ((Owner) myAgent).createAndSendReply(ACLMessage.AGREE, receivedMsg);
                
                List<String> smartHomesNames = ((Owner) myAgent).getSmartHomeNames();
                for(int i = 0; i < smartHomesNames.size(); i++) {
                    String curName = smartHomesNames.get(i);
                    if(curName.equals(homeName)) {
                        jsonObject.remove(MessageUtil.SMART_HOME);
                        ((Owner) myAgent).createAndSend(ACLMessage.REQUEST, curName, jsonObject, receivedMsg.getConversationId());
                    }
                }
                ((Owner) myAgent).log("Sent Update Routine Request");
				
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
