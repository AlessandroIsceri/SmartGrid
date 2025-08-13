package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.NonRenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent.SmartHomeStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UpdateNonRenewableStatus extends CustomBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private boolean finished;

    public UpdateNonRenewableStatus(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent){
        super(nonRenewablePowerPlantAgent);
        this.finished = false;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("Received a message FROM " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            boolean on = (boolean) jsonObject.get(MessageUtil.ON);
            NonRenewablePowerPlant nonRenewablePowerPlant = ((NonRenewablePowerPlantAgent) myAgent).getNonRenewablePowerPlant();
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
