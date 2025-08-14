package com.ii.smartgrid.smartgrid.behaviours.smartbuilding;


import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithBattery;
import com.ii.smartgrid.smartgrid.model.SmartBuilding;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyRequestToGridBehaviour extends CustomOneShotBehaviour{

    private SmartBuildingAgent smartBuildingAgent;
    public SendEnergyRequestToGridBehaviour(SmartBuildingAgent smartBuildingAgent){
        super(smartBuildingAgent);
        this.smartBuildingAgent = smartBuildingAgent;
    }

    @Override
    public void action() {
        SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding(); 
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //può rilasciare energia se ne ha troppa e non gli serve
		double expectedConsumption = smartBuilding.getExpectedConsumption();
        double availableEnergy = smartBuilding.getExpectedProduction();
        TransactionType transactionType;
        // Request energy from the grid
        // String conversationId = null;
        String gridName = smartBuilding.getGridName();
        double energy = Math.abs(expectedConsumption - availableEnergy);
        if(availableEnergy >= expectedConsumption){
            transactionType = TransactionType.SEND;
            Cable cable = smartBuilding.getCable(gridName);
            energy = cable.computeTransmittedPower(energy);
            // conversationId = "release-"+customAgent.getLocalName();
        } else {
            transactionType = TransactionType.RECEIVE; 
            // conversationId = "request-"+customAgent.getLocalName();
        }
        EnergyTransaction energyTransaction = new EnergyTransactionWithoutBattery(smartBuilding.getPriority(), energy, customAgent.getLocalName(), transactionType);
        Map<String, Object> content = new HashMap<String, Object>();

        ObjectMapper objectMapper = customAgent.getObjectMapper();
        JsonNode node = objectMapper.valueToTree(energyTransaction); // include @JsonTypeInfo
        content.put(MessageUtil.ENERGY_TRANSACTION, node);
        
        // content.put(MessageUtil.OPERATION, MessageUtil.CONSUME);
        content.put(MessageUtil.BLACKOUT, false);

        customAgent.createAndSend(ACLMessage.REQUEST, gridName, content); //conversationId);
        //TODO REMOVE
        // SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();
        log("*****" + smartBuilding.toString());
	} 
}