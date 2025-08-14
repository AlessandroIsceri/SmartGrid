package com.ii.smartgrid.smartgrid.behaviours.smartbuilding;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent.SmartBuildingStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.SmartBuilding;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
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
            log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName());
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            double energy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
            if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                if(operation.equals(MessageUtil.CONSUME)){
                    log("Energy consumed");
                }else{
                    log("Error: invalid operation");
                }
            } else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                //possible blackout 
                if(operation.equals(MessageUtil.CONSUME)){
                    log("ATTENTION: Blackout soon");
                    smartBuildingAgent.getSmartBuilding().shutDown();
                    smartBuildingAgent.setBuildingStatus(SmartBuildingStatus.BLACKOUT);
                }else{
                    log("Error: invalid operation");
                }
            }
            //TODO REMOVE
            SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();
            log("*****" + smartBuilding.toString());
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
