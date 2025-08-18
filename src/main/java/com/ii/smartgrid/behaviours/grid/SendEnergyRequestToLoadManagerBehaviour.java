package com.ii.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.agents.GridAgent;
import com.ii.smartgrid.agents.GridAgent.GridStatus;
import com.ii.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.model.Battery;
import com.ii.smartgrid.model.entities.Grid;
import com.ii.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.model.routing.EnergyTransactionWithBattery;
import com.ii.smartgrid.model.routing.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.model.routing.EnergyTransaction.TransactionType;
import com.ii.smartgrid.utils.MessageUtil;

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
        Map<String, Object> content = new HashMap<>();
        EnergyTransaction energyTransaction = null;

        double energyTransactionValue = grid.getExpectedProduction() - grid.getExpectedConsumption();
        TransactionType energyTransactionType;
        if(energyTransactionValue >= 0){
            // Sender (producer) node
            energyTransactionType = TransactionType.SEND;
            gridAgent.setGridStatus(GridStatus.SEND);
        }else{
            // Receiver (consumer) node
            energyTransactionType = TransactionType.RECEIVE;
            gridAgent.setGridStatus(GridStatus.RECEIVE);
        }

        Battery battery = grid.getBattery();
        if(battery != null){
            energyTransaction = new EnergyTransactionWithBattery(grid.getPriority(), Math.abs(energyTransactionValue), customAgent.getLocalName(), battery, energyTransactionType);
        }else{
            energyTransaction = new EnergyTransactionWithoutBattery(grid.getPriority(), Math.abs(energyTransactionValue), customAgent.getLocalName(), energyTransactionType);
        }
        ObjectMapper objectMapper = customAgent.getObjectMapper();
        JsonNode node = objectMapper.valueToTree(energyTransaction);
        content.put(MessageUtil.ENERGY_TRANSACTION, node);
        
        customAgent.createAndSend(ACLMessage.REQUEST, loadManagerName, content);
    }

}
