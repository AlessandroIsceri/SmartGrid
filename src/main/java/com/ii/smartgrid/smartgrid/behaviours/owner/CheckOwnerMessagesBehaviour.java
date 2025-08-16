package com.ii.smartgrid.smartgrid.behaviours.owner;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.OwnerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomCyclicBehaviour;
import com.ii.smartgrid.smartgrid.model.entities.Owner;
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
				Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
				String buildingName = (String) jsonObject.get(MessageUtil.SMART_BUILDING);
                customAgent.createAndSendReply(ACLMessage.AGREE, receivedMsg);
                
                Owner owner = ownerAgent.getOwner();
                List<String> smartBuildingsNames = owner.getSmartBuildingNames();
                for (String curName : smartBuildingsNames) {
                    if (curName.equals(buildingName)) {
                        jsonObject.remove(MessageUtil.SMART_BUILDING);
                        // Send the request to update the smartbuilding routine 
                        customAgent.createAndSend(ACLMessage.REQUEST, curName, jsonObject, receivedMsg.getConversationId());
                    }
                }
                log("Sent Update Routine Request");
				
			}else if(receivedMsg.getPerformative() == ACLMessage.AGREE){
				log("Received agree for " + receivedMsg.getConversationId());
			}else if(receivedMsg.getPerformative() == ACLMessage.INFORM){
				log("Received inform for " + receivedMsg.getConversationId());
			}
		}else {
			block();
		}
	}
}
