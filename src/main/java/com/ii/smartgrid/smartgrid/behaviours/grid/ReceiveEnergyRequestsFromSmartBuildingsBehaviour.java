package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyRequestsFromSmartBuildingsBehaviour extends CustomBehaviour{
    private int requestCont = 0;
    private boolean finished = false;
    private int smartBuildingsCount = 0;
    private GridAgent gridAgent;

    public ReceiveEnergyRequestsFromSmartBuildingsBehaviour(GridAgent gridAgent){
        super(gridAgent);
        this.smartBuildingsCount = gridAgent.getGrid().getSmartBuildingNames().size();
        this.gridAgent = gridAgent;
    }

    @Override
    public void action() {

        if(smartBuildingsCount == 0){
            this.finished = true;
            return;
        }

		//request from non black out, inform from black out buildings
		MessageTemplate mt1 = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), 
                                                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        
        Grid grid = gridAgent.getGrid();
        List<String> smartBuildingNames = grid.getSmartBuildingNames();

        MessageTemplate mt;
        if(smartBuildingNames.isEmpty()){
            mt = mt1;
        }else{
            MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(smartBuildingNames.get(0), AID.ISLOCALNAME));
            for(int i = 1; i < smartBuildingNames.size(); i++){
                String smartBuildingName = smartBuildingNames.get(i);
                mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(smartBuildingName, AID.ISLOCALNAME)));
            }
            mt = MessageTemplate.and(mt1, mt2); 
        }

        
        
        ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName());
            requestCont++;

            String sender = receivedMsg.getSender().getLocalName();
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            if(receivedMsg.getPerformative() == ACLMessage.REQUEST){
				/**
				 * {
				 * 		"energyTransaction": 200.0,
                 *      "blackout" : true/false
				 * }
				 */
                // String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                
                EnergyTransaction energyTransaction  = customAgent.readValueFromJson(jsonObject.get(MessageUtil.ENERGY_TRANSACTION), EnergyTransaction.class);
                TransactionType transactionType = energyTransaction.getTransactionType();
                if(transactionType == TransactionType.RECEIVE) {
                    double requestedEnergy = energyTransaction.getEnergyTransactionValue();
                    grid.addExpectedConsumption(requestedEnergy);
                    grid.addEnergyRequest(sender, energyTransaction);
                    grid.updateGridPriority(energyTransaction.getPriority());
                    log("Requested Energy: " + requestedEnergy);
                } else {
                    double releasedEnergy = energyTransaction.getEnergyTransactionValue();
                    log("Released Energy: " + releasedEnergy);
                    // grid.removeExpectedConsumption(releasedEnergy);
                    grid.addExpectedProduction(releasedEnergy);
                    if(grid.containsSmartBuildingWithoutPower(sender)){
                        // false -> energy restored independently
                        // true -> building still in blackout		
                        boolean blackout = (boolean) jsonObject.get(MessageUtil.BLACKOUT);
                        if(!blackout){
                            grid.removeSmartBuildingWithoutPower(sender);
                        }
                    }
                }
            }else if(receivedMsg.getPerformative() == ACLMessage.INFORM){
                //{"blackout": true/false}
				// false -> energy restored independently
				// true -> building still in blackout
                // String conversationId = receivedMsg.getConversationId();
                boolean blackout = (boolean) jsonObject.get(MessageUtil.BLACKOUT);
                if(!blackout){
                    grid.removeSmartBuildingWithoutPower(sender);
                }
            }
            if(requestCont < this.smartBuildingsCount){
                customAgent.blockBehaviourIfQueueIsEmpty(this);
            }else{
                finished = true;
            }
		} else {
			customAgent.blockBehaviourIfQueueIsEmpty(this);
		}
    }

    @Override
    public boolean done() {
        return finished;
    }
}
