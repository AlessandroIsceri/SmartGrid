package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent.GridStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithBattery;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyRequestToLoadManagerBehaviour extends CustomOneShotBehaviour{

    private GridAgent gridAgent;

    public SendEnergyRequestToLoadManagerBehaviour(GridAgent gridAgent) {
        super(gridAgent);
        this.gridAgent = gridAgent;
    }

    @Override
    public void action() {
        Grid grid = gridAgent.getGrid();
        String loadManagerName = grid.getLoadManagerName();
        Map<String, Object> content = new HashMap<String, Object>();
        EnergyTransaction energyTransaction = null;
        Battery battery = grid.getBattery();

        double energyTransactionValue = grid.getExpectedProduction() - grid.getExpectedConsumption();
        TransactionType energyTransactionType;
        if(energyTransactionValue >= 0){
            //producer
            energyTransactionType = TransactionType.SEND;
            gridAgent.setGridStatus(GridStatus.SEND);
        }else{
            energyTransactionType = TransactionType.RECEIVE;
            gridAgent.setGridStatus(GridStatus.RECEIVE);
        }

        if(battery != null){
            energyTransaction = new EnergyTransactionWithBattery(grid.getPriority(), Math.abs(energyTransactionValue), customAgent.getLocalName(), battery, energyTransactionType);
        }else{
            energyTransaction = new EnergyTransactionWithoutBattery(grid.getPriority(), Math.abs(energyTransactionValue), customAgent.getLocalName(), energyTransactionType);
        }
        ObjectMapper objectMapper = customAgent.getObjectMapper();
        JsonNode node = objectMapper.valueToTree(energyTransaction); // include @JsonTypeInfo
        content.put(MessageUtil.ENERGY_TRANSACTION, node);
        
        customAgent.createAndSend(ACLMessage.REQUEST, loadManagerName, content);
    }

}
