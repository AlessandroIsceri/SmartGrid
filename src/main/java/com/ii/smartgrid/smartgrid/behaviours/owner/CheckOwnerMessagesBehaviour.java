package com.ii.smartgrid.smartgrid.behaviours.owner;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.OwnerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.smartgrid.model.Owner;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;

public class CheckOwnerMessagesBehaviour extends CustomCyclicBehaviour {
	
	private OwnerAgent ownerAgent;

	public CheckOwnerMessagesBehaviour(OwnerAgent ownerAgent) {
		super(ownerAgent);
		this.ownerAgent = ownerAgent;
	}
	
	@Override
	public void action() {
		ACLMessage receivedMsg = customAgent.receive();
		if (receivedMsg != null) {
			if (receivedMsg.getPerformative() == ACLMessage.REQUEST) {
				/**
				 * {
				 * 		"operation": "add"/"remove"
				 * 		"smartBuilding": smartBuildingAID.getName()
				 * 		"tasks": task[] sempre JSON 
				 * }
				 */
				Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
				String buildingName = (String) jsonObject.get(MessageUtil.SMART_BUILDING);
                customAgent.createAndSendReply(ACLMessage.AGREE, receivedMsg);
                
                Owner owner = ownerAgent.getOwner();
                List<String> smartBuildingsNames = owner.getSmartBuildingNames();
                for(int i = 0; i < smartBuildingsNames.size(); i++) {
                    String curName = smartBuildingsNames.get(i);
                    if(curName.equals(buildingName)) {
                        jsonObject.remove(MessageUtil.SMART_BUILDING);
                        customAgent.createAndSend(ACLMessage.REQUEST, curName, jsonObject, receivedMsg.getConversationId());
                    }
                }
                log("Sent Update Routine Request");
				
			}else if(receivedMsg.getPerformative() == ACLMessage.AGREE){
				//owner -> manda msg request -> smartBuilding riceve, manda agree ed esegue -> inform è andata bene
				log("RECEIVED AGREE FOR " + receivedMsg.getConversationId());
			}else if(receivedMsg.getPerformative() == ACLMessage.INFORM){
				//msg fatto da noi avrà un conversationId -> che viene mantenuto tra i msg
				log("INFORM RECEIVED FOR " + receivedMsg.getConversationId());
			}
		}else {
			block();
		}
	}
}
