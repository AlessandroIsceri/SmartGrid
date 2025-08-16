package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.NonRenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

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
		// Update internal status if this powerplant has to be turned ON or OFF following given instructions
        if (receivedMsg != null) {
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
