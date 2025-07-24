package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyRequestsFromSmartHomesBehaviour extends Behaviour{
    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private int requestCont = 0;
    private boolean finished = false;

    public ReceiveEnergyRequestsFromSmartHomesBehaviour(GridAgent gridAgent){
        super(gridAgent);
    }

    @Override
    public void action() {
        ((CustomAgent) myAgent).log("sono stato svegliato (ReceiveEnergyRequestsFromSmartHomesBehaviour)", BEHAVIOUR_NAME);
		//request from non black out, inform from black out homes
		MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), 
                                                MessageTemplate.MatchPerformative(ACLMessage.INFORM));		
        ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("ho ricevuto una richiesta", BEHAVIOUR_NAME);
            requestCont++;
            
            Grid grid = ((GridAgent) myAgent).getGrid();

            String sender = receivedMsg.getSender().getLocalName();
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            if(receivedMsg.getPerformative() == ACLMessage.REQUEST){
				/**
				 * {
				 * 		"operation": "release"/"consume"
				 * 		"energy": 200.0
				 * }
				 */
                String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                
                if(operation.equals(MessageUtil.CONSUME)) {
                    double requestedEnergy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
                    grid.addExpectedConsumption(requestedEnergy);
                    grid.addEnergyRequest(sender, requestedEnergy);
                    ((CustomAgent) myAgent).log("Requested Energy: " + requestedEnergy, BEHAVIOUR_NAME);
                } else {
                    ((CustomAgent) myAgent).log("Error: invalid parameter \"operation\": " + operation, BEHAVIOUR_NAME);
                }
            }else if(receivedMsg.getPerformative() == ACLMessage.INFORM){
                //{"blackout": true/false}
				// false -> energy restored independently
				// true -> home still in blackout
                String conversationId = receivedMsg.getConversationId();
                
                String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                if(conversationId.contains(MessageUtil.BLACKOUT)){
                    boolean blackout = (boolean) jsonObject.get(MessageUtil.BLACKOUT);
                    if(!blackout){
                        grid.removeSmartHomeWithoutPower(sender);
                    }
                } else{ 
                    double releasedEnergy = (double) jsonObject.get(MessageUtil.RELEASED_ENERGY);
                    ((CustomAgent) myAgent).log("Released Energy: " + releasedEnergy, BEHAVIOUR_NAME);
                    grid.removeExpectedConsumption(releasedEnergy);
                    if(grid.containsSmartHomeWithoutPower(sender)){
                        // false -> energy restored independently
                        // true -> home still in blackout		
                        boolean blackout = (boolean) jsonObject.get(MessageUtil.BLACKOUT);
                        if(!blackout){
                            grid.removeSmartHomeWithoutPower(sender);
                        }
                    }
                }
            }
            int smartHomesCount = grid.getSmartHomeNames().size();
            if(requestCont < smartHomesCount){
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                finished = true;
            }
		} else {
			((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
		}
    }

    @Override
    public boolean done() {
        return finished;
    }
}
