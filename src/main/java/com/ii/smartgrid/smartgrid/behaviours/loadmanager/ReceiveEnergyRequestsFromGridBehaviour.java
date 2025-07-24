package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.model.LoadManager;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyRequestsFromGridBehaviour extends Behaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private boolean finished = false;
    private int requestCont = 0;
    private int gridCount;

    public ReceiveEnergyRequestsFromGridBehaviour (LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        gridCount = ((LoadManagerAgent) myAgent).getLoadManager().getGridNames().size();
    }

    @Override
    public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("ho ricevuto una richiesta (ReceiveEnergyRequestsFromGridBehaviour)", BEHAVIOUR_NAME);
            requestCont++;
			/**
			 * {
			 * 		"requestedEnergy": 200.0
			 * }
			 */
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            double requestedEnergy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
            ((CustomAgent) myAgent).log(MessageUtil.REQUESTED_ENERGY + requestedEnergy, BEHAVIOUR_NAME);
            String sender = receivedMsg.getSender().getLocalName();
            LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager(); 
            loadManager.addGridRequestedEnergy(sender, requestedEnergy);
            loadManager.addExpectedConsumption(requestedEnergy);

            if(requestCont < gridCount){
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                finished = true;
            }
		} else {
			((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
		}
    }

    @Override
    public boolean done() {
        return finished;
    }
}
