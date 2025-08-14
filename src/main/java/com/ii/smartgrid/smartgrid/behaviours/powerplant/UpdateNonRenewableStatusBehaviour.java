package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.NonRenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent.SmartBuildingStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UpdateNonRenewableStatusBehaviour extends CustomBehaviour{

    private boolean finished;
    private NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent;

    public UpdateNonRenewableStatusBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent){
        super(nonRenewablePowerPlantAgent);
        this.finished = false;
        this.nonRenewablePowerPlantAgent = nonRenewablePowerPlantAgent;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            log("Received a message FROM " + receivedMsg.getSender().getLocalName());
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            boolean on = (boolean) jsonObject.get(MessageUtil.ON);
            NonRenewablePowerPlant nonRenewablePowerPlant = nonRenewablePowerPlantAgent.getNonRenewablePowerPlant();
            nonRenewablePowerPlant.setOn(on);
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
