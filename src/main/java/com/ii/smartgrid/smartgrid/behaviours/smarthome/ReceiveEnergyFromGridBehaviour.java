package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyProducer;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
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
	
    private boolean finished = false;

	public ReceiveEnergyFromGridBehaviour(SmartHome smartHome) {
		super(smartHome);
	}

	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), 
                                                MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        ACLMessage receivedMsg = myAgent.receive();
		if (receivedMsg != null) {
            ((SmartHome) myAgent).log("ReceiveEnergyFromGridBehaviour received a message");
            Map<String, Object> jsonObject = ((SmartHome) myAgent).convertAndReturnContent(receivedMsg);
            if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                double energy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
                if(operation.equals(MessageUtil.CONSUME)){
                    ((SmartHome) myAgent).log("Energy consumed");
                }else{
                    ((SmartHome) myAgent).log("Error: invalid operation");
                }
            } else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                //possible blackout 
                String operation = (String) jsonObject.get(MessageUtil.OPERATION);
                double energy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
                if(operation.equals(MessageUtil.CONSUME)){
                    ((SmartHome) myAgent).log("ATTENTION: Blackout soon");
                    ((SmartHome) myAgent).shutDown();
                }else{
                    ((SmartHome) myAgent).log("Error: invalid operation");
                }
            } else {
                block();
            }
		} else {
			block();
		}
        ((SmartHome) myAgent).log("ReceiveEnergyFromGridBehaviour finished");
        finished = true;
	}

    @Override
    public boolean done() {
        return finished;
    }

}
