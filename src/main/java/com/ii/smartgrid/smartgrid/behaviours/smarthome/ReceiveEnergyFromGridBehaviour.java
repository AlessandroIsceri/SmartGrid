package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent.SmartHomeStatus;
import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromGridBehaviour extends Behaviour{
    
    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private boolean finished = false;

	public ReceiveEnergyFromGridBehaviour(SmartHomeAgent smartHomeAgent) {
		super(smartHomeAgent);
	}

	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), 
                                                MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            double energy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
            if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                if(operation.equals(MessageUtil.CONSUME)){
                    ((CustomAgent) myAgent).log("Energy consumed", BEHAVIOUR_NAME);
                }else{
                    ((CustomAgent) myAgent).log("Error: invalid operation", BEHAVIOUR_NAME);
                }
            } else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                //possible blackout 
                if(operation.equals(MessageUtil.CONSUME)){
                    ((CustomAgent) myAgent).log("ATTENTION: Blackout soon", BEHAVIOUR_NAME);
                    ((SmartHomeAgent) myAgent).getSmartHome().shutDown();
                    ((SmartHomeAgent) myAgent).setHomeStatus(SmartHomeStatus.BLACKOUT);
                }else{
                    ((CustomAgent) myAgent).log("Error: invalid operation", BEHAVIOUR_NAME);
                }
            }
            //TODO REMOVE
            SmartHome smartHome = ((SmartHomeAgent) myAgent).getSmartHome();
            ((CustomAgent) myAgent).log("*****" + smartHome.toString(), BEHAVIOUR_NAME);
             ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
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
