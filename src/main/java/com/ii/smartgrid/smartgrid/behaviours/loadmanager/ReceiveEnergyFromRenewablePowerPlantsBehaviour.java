package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.beans.Customizer;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromRenewablePowerPlantsBehaviour extends Behaviour{
    
    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();


    private int requestCont = 0;
    private boolean finished = false;
    private int renewablePowerPlantCount; 

    public ReceiveEnergyFromRenewablePowerPlantsBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        renewablePowerPlantCount = ((LoadManagerAgent) myAgent).getLoadManager().getRenewablePowerPlantNames().size();
    }

    @Override
    public void action() {
        ((CustomAgent) myAgent).log("sono stato svegliato (ReceiveEnergyFromRenewablePowerPlantsBehaviour)", BEHAVIOUR_NAME);
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("ho ricevuto una richiesta", BEHAVIOUR_NAME);
            requestCont++;
			/**
			 * {
			 * 		"energy": 200.0
			 * }
			 */
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
            ((CustomAgent) myAgent).log("receivedEnergy: " + receivedEnergy, BEHAVIOUR_NAME);
            
            LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
            loadManager.removeExpectedConsumption(receivedEnergy);

            if(requestCont < renewablePowerPlantCount){
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                //se l'energia prodotta dalle energia rinnovabili è sufficiente, carico la batteria
                if(loadManager.getExpectedConsumption() < 0){
                    double extraEnergy = -loadManager.getExpectedConsumption();
                    loadManager.getBattery().fillBattery(extraEnergy);
                }
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
