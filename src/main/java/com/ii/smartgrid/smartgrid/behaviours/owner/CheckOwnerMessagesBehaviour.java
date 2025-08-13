package com.ii.smartgrid.smartgrid.behaviours.owner;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.OwnerAgent;
import com.ii.smartgrid.smartgrid.model.Owner;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class CheckOwnerMessagesBehaviour extends CyclicBehaviour {
	
	private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();
	
	public CheckOwnerMessagesBehaviour(OwnerAgent ownerAgent) {
		super(ownerAgent);
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
				Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
				String homeName = (String) jsonObject.get(MessageUtil.SMART_HOME);
                ((CustomAgent) myAgent).createAndSendReply(ACLMessage.AGREE, receivedMsg);
                
                Owner owner = ((OwnerAgent) myAgent).getOwner();
                List<String> smartHomesNames = owner.getSmartHomeNames();
                for(int i = 0; i < smartHomesNames.size(); i++) {
                    String curName = smartHomesNames.get(i);
                    if(curName.equals(homeName)) {
                        jsonObject.remove(MessageUtil.SMART_HOME);
                        ((CustomAgent) myAgent).createAndSend(ACLMessage.REQUEST, curName, jsonObject, receivedMsg.getConversationId());
                    }
                }
                ((CustomAgent) myAgent).log("Sent Update Routine Request", BEHAVIOUR_NAME);
				
			}else if(receivedMsg.getPerformative() == ACLMessage.AGREE){
				//owner -> manda msg request -> smartHome riceve, manda agree ed esegue -> inform è andata bene
				((CustomAgent) myAgent).log("RECEIVED AGREE FOR " + receivedMsg.getConversationId(), BEHAVIOUR_NAME);
			}else if(receivedMsg.getPerformative() == ACLMessage.INFORM){
				//msg fatto da noi avrà un conversationId -> che viene mantenuto tra i msg
				((CustomAgent) myAgent).log("INFORM RECEIVED FOR " + receivedMsg.getConversationId(), BEHAVIOUR_NAME);
			}
		}else {
			block();
		}
	}
}
