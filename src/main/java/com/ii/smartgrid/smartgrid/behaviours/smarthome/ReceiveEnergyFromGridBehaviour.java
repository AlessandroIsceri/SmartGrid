package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.sniffer.Message;

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
        ACLMessage receivedMsg = myAgent.receive();
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("ReceiveEnergyFromGridBehaviour received a message", BEHAVIOUR_NAME);
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                double energy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
                if(operation.equals(MessageUtil.CONSUME)){
                    ((CustomAgent) myAgent).log("Energy consumed", BEHAVIOUR_NAME);
                }else{
                    ((CustomAgent) myAgent).log("Error: invalid operation", BEHAVIOUR_NAME);
                }
            } else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                //possible blackout 
                String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                double energy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
                if(operation.equals(MessageUtil.CONSUME)){
                    ((CustomAgent) myAgent).log("ATTENTION: Blackout soon", BEHAVIOUR_NAME);
                    ((SmartHomeAgent) myAgent).getSmartHome().shutDown();
                }else{
                    ((CustomAgent) myAgent).log("Error: invalid operation", BEHAVIOUR_NAME);
                }
            } else {
                block();
            }
		} else {
			block();
		}
        ((CustomAgent) myAgent).log("ReceiveEnergyFromGridBehaviour finished", BEHAVIOUR_NAME);
        finished = true;
	}

    @Override
    public boolean done() {
        return finished;
    }

}
