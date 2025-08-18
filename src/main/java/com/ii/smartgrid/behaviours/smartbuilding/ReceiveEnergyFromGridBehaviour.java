package com.ii.smartgrid.behaviours.smartbuilding;

import java.util.Map;

import com.ii.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.agents.SmartBuildingAgent.SmartBuildingStatus;
import com.ii.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.model.entities.SmartBuilding;
import com.ii.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromGridBehaviour extends CustomBehaviour{
    
    private SmartBuildingAgent smartBuildingAgent;
    private boolean finished = false;
    
	public ReceiveEnergyFromGridBehaviour(SmartBuildingAgent smartBuildingAgent) {
		super(smartBuildingAgent);
        this.smartBuildingAgent = smartBuildingAgent;
	}

	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), 
                                                MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            double energy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
            double expectedConsumption = smartBuilding.getExpectedConsumption();
            double availableEnergy = smartBuilding.getExpectedProduction();
            double requestedEnergy = Math.abs(expectedConsumption - availableEnergy);
            double extraEnergy = energy - requestedEnergy;
            // Agree -> The grid sent the needed amount of energy
            if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                if(operation.equals(MessageUtil.CONSUME)){
                    smartBuilding.fillBattery(extraEnergy);
                    log("Energy consumed");
                }else{
                    log("Error: invalid operation");
                }
            } else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                // Refuse -> The grid couldn't sent the needed amount of energy
                if(operation.equals(MessageUtil.CONSUME)){
                    log("The grid couldn't satisfy the request (possible blackout incoming)");
                    smartBuildingAgent.getSmartBuilding().shutDown();
                    smartBuildingAgent.setBuildingStatus(SmartBuildingStatus.BLACKOUT);
                }else{
                    log("Error: invalid operation");
                }
            }
            finished = true;
		} else {
			block();
		}
	}

    @Override
    public boolean done() {
        return finished;
    }
}
