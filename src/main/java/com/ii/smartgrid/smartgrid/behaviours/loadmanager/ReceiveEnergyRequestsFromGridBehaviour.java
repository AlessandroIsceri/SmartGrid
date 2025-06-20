package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.LoadManager;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.agents.LoadManager;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyRequestsFromGridBehaviour extends Behaviour{
    
    private boolean finished = false;
    private int requestCont = 0;
    private int gridCount = ((LoadManager) myAgent).getGridNames().size();

    public ReceiveEnergyRequestsFromGridBehaviour (LoadManager loadManager){
        super(loadManager);
    }

    @Override
    public void action() {
        ((LoadManager) myAgent).log("sono stato svegliato (ReceiveGridRequestsBehaviour)");
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((LoadManager) myAgent).log("ho ricevuto una richiesta");
            requestCont++;
			/**
			 * {
			 * 		"requestedEnergy": 200.0
			 * }
			 */
            Map<String, Object> jsonObject = ((LoadManager) myAgent).convertAndReturnContent(receivedMsg);
            double requestedEnergy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
            ((LoadManager) myAgent).log(MessageUtil.REQUESTED_ENERGY + requestedEnergy);
            String sender = receivedMsg.getSender().getLocalName();
            ((LoadManager) myAgent).addGridRequestedEnergy(sender, requestedEnergy);
            ((LoadManager) myAgent).addExpectedConsumption(requestedEnergy);

            if(requestCont < gridCount){
                block();
            }else{
                finished = true;
            }
		} else {
			block();
		}
    }

    @Override
    public boolean done() {
        return finished;
    }
}
