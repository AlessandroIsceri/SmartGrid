package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.LoadManager;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.agents.LoadManager;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromRenewablePowerPlantsBehaviour extends Behaviour{
    
    private int requestCont = 0;
    private boolean finished = false;
    private int renewablePowerPlantCount = ((LoadManager) myAgent).getRenewablePowerPlantNames().size();

    public ReceiveEnergyFromRenewablePowerPlantsBehaviour(LoadManager loadManager){
        super(loadManager);
    }

    @Override
    public void action() {
        ((LoadManager) myAgent).log("sono stato svegliato (ReceiveRenewablePowerPlantBehaviour)");
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((LoadManager) myAgent).log("ho ricevuto una richiesta");
            requestCont++;
			/**
			 * {
			 * 		"energy": 200.0
			 * }
			 */
            Map<String, Object> jsonObject = ((LoadManager) myAgent).convertAndReturnContent(receivedMsg);
            double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
            ((LoadManager) myAgent).log("receivedEnergy: " + receivedEnergy);
            ((LoadManager) myAgent).removeExpectedConsumption(receivedEnergy);

            if(requestCont < renewablePowerPlantCount){
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
