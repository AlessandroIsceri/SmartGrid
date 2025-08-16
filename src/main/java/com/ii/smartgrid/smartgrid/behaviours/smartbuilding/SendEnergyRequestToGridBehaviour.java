package com.ii.smartgrid.smartgrid.behaviours.smartbuilding;


import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.entities.SmartBuilding;
import com.ii.smartgrid.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.routing.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.smartgrid.model.routing.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

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
        // Checks the amount of energy produced and consumed during this turn
		double expectedConsumption = smartBuilding.getExpectedConsumption();
        double availableEnergy = smartBuilding.getExpectedProduction();
        String gridName = smartBuilding.getGridName();
        TransactionType transactionType;
        double energy = Math.abs(expectedConsumption - availableEnergy);
        // If the production is greater than consumption, send excess energy 
        // Else, send a request containing the needed amount of energy to the grid
        if(availableEnergy >= expectedConsumption){
            transactionType = TransactionType.SEND;
            Cable cable = smartBuilding.getCable(gridName);
            energy = cable.computeTransmittedPower(energy);
        } else {
            transactionType = TransactionType.RECEIVE; 
        }
        EnergyTransaction energyTransaction = new EnergyTransactionWithoutBattery(smartBuilding.getPriority(), energy, customAgent.getLocalName(), transactionType);
        Map<String, Object> content = new HashMap<>();

        ObjectMapper objectMapper = customAgent.getObjectMapper();
        JsonNode node = objectMapper.valueToTree(energyTransaction);
        content.put(MessageUtil.ENERGY_TRANSACTION, node);
        
        content.put(MessageUtil.BLACKOUT, false);

        customAgent.createAndSend(ACLMessage.REQUEST, gridName, content);
	} 
}