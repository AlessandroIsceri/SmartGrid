package com.ii.smartgrid.smartgrid.behaviours.smarthome;


import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithBattery;
import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyRequestToGridBehaviour extends CustomOneShotBehaviour{

    public SendEnergyRequestToGridBehaviour(SmartHomeAgent smartHomeAgent){
        super(smartHomeAgent);
    }

    @Override
    public void action() {
        SmartHome smartHome = ((SmartHomeAgent) myAgent).getSmartHome(); 
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //può rilasciare energia se ne ha troppa e non gli serve
		double expectedConsumption = smartHome.getExpectedConsumption();
        double availableEnergy = smartHome.getExpectedProduction();
        TransactionType transactionType;
        // Request energy from the grid
        // String conversationId = null;
        String gridName = smartHome.getGridName();
        double energy = Math.abs(expectedConsumption - availableEnergy);
        if(availableEnergy >= expectedConsumption){
            transactionType = TransactionType.SEND;
            Cable cable = smartHome.getCable(gridName);
            energy = cable.computeTransmittedPower(energy);
            // conversationId = "release-"+myAgent.getLocalName();
        } else {
            transactionType = TransactionType.RECEIVE; 
            // conversationId = "request-"+myAgent.getLocalName();
        }
        EnergyTransaction energyTransaction = new EnergyTransactionWithoutBattery(smartHome.getPriority(), energy, myAgent.getLocalName(), transactionType);
        Map<String, Object> content = new HashMap<String, Object>();

        ObjectMapper objectMapper = ((CustomAgent) myAgent).getObjectMapper();
        JsonNode node = objectMapper.valueToTree(energyTransaction); // include @JsonTypeInfo
        content.put(MessageUtil.ENERGY_TRANSACTION, node);
        
        // content.put(MessageUtil.OPERATION, MessageUtil.CONSUME);
        content.put(MessageUtil.BLACKOUT, false);

        ((CustomAgent) myAgent).createAndSend(ACLMessage.REQUEST, gridName, content); //conversationId);
        //TODO REMOVE
        // SmartHome smartHome = ((SmartHomeAgent) myAgent).getSmartHome();
        ((CustomAgent) myAgent).log("*****" + smartHome.toString(), BEHAVIOUR_NAME);
	} 
}