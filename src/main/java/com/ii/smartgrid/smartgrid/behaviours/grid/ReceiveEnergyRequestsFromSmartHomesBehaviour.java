package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyRequestsFromSmartHomesBehaviour extends Behaviour{
    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private int requestCont = 0;
    private boolean finished = false;
    private int smartHomesCount = 0;

    public ReceiveEnergyRequestsFromSmartHomesBehaviour(GridAgent gridAgent){
        super(gridAgent);
        this.smartHomesCount = gridAgent.getGrid().getSmartHomeNames().size();
    }

    @Override
    public void action() {

        if(smartHomesCount == 0){
            this.finished = true;
            return;
        }

		//request from non black out, inform from black out homes
		MessageTemplate mt1 = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), 
                                                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        
        Grid grid = ((GridAgent) myAgent).getGrid();
        List<String> smartHomeNames = grid.getSmartHomeNames();

        MessageTemplate mt;
        if(smartHomeNames.isEmpty()){
            mt = mt1;
        }else{
            MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(smartHomeNames.get(0), AID.ISLOCALNAME));
            for(int i = 1; i < smartHomeNames.size(); i++){
                String smartHomeName = smartHomeNames.get(i);
                mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(smartHomeName, AID.ISLOCALNAME)));
            }
            mt = MessageTemplate.and(mt1, mt2); 
        }

        
        
        ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);
            requestCont++;

            String sender = receivedMsg.getSender().getLocalName();
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            if(receivedMsg.getPerformative() == ACLMessage.REQUEST){
				/**
				 * {
				 * 		"energyTransaction": 200.0,
                 *      "blackout" : true/false
				 * }
				 */
                // String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                
                EnergyTransaction energyTransaction  = ((CustomAgent) myAgent).readValueFromJson(jsonObject.get(MessageUtil.ENERGY_TRANSACTION), EnergyTransaction.class);
                TransactionType transactionType = energyTransaction.getTransactionType();
                if(transactionType == TransactionType.RECEIVE) {
                    double requestedEnergy = energyTransaction.getEnergyTransactionValue();
                    grid.addExpectedConsumption(requestedEnergy);
                    grid.addEnergyRequest(sender, energyTransaction);
                    grid.updateGridPriority(energyTransaction.getPriority());
                    ((CustomAgent) myAgent).log("Requested Energy: " + requestedEnergy, BEHAVIOUR_NAME);
                } else {
                    double releasedEnergy = energyTransaction.getEnergyTransactionValue();
                    ((CustomAgent) myAgent).log("Released Energy: " + releasedEnergy, BEHAVIOUR_NAME);
                    // grid.removeExpectedConsumption(releasedEnergy);
                    grid.addExpectedProduction(releasedEnergy);
                    if(grid.containsSmartHomeWithoutPower(sender)){
                        // false -> energy restored independently
                        // true -> home still in blackout		
                        boolean blackout = (boolean) jsonObject.get(MessageUtil.BLACKOUT);
                        if(!blackout){
                            grid.removeSmartHomeWithoutPower(sender);
                        }
                    }
                }
            }else if(receivedMsg.getPerformative() == ACLMessage.INFORM){
                //{"blackout": true/false}
				// false -> energy restored independently
				// true -> home still in blackout
                // String conversationId = receivedMsg.getConversationId();
                boolean blackout = (boolean) jsonObject.get(MessageUtil.BLACKOUT);
                if(!blackout){
                    grid.removeSmartHomeWithoutPower(sender);
                }
            }
            if(requestCont < this.smartHomesCount){
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
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
