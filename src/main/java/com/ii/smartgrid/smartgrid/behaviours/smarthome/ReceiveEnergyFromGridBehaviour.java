package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent.SmartHomeStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromGridBehaviour extends CustomBehaviour{
    
    private SmartHomeAgent smartHomeAgent;
    private boolean finished = false;
    
	public ReceiveEnergyFromGridBehaviour(SmartHomeAgent smartHomeAgent) {
		super(smartHomeAgent);
        this.smartHomeAgent = smartHomeAgent;
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
                    smartHomeAgent.getSmartHome().shutDown();
                    smartHomeAgent.setHomeStatus(SmartHomeStatus.BLACKOUT);
                }else{
                    log("Error: invalid operation");
                }
            }
            //TODO REMOVE
            SmartHome smartHome = smartHomeAgent.getSmartHome();
            log("*****" + smartHome.toString());
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
