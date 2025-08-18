package com.ii.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.agents.GridAgent;
import com.ii.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.model.Cable;
import com.ii.smartgrid.model.entities.Grid;
import com.ii.smartgrid.model.entities.CustomObject.Priority;
import com.ii.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.model.routing.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.model.routing.EnergyTransaction.TransactionType;
import com.ii.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;

public class SendEnergyToSmartBuildingsBehaviour extends CustomOneShotBehaviour{

    private GridAgent gridAgent;

    public SendEnergyToSmartBuildingsBehaviour(GridAgent gridAgent){
        super(gridAgent);
        this.gridAgent = gridAgent;
    }

    @Override
    public void action() {
        Grid grid = gridAgent.getGrid();

        // Energy sent to the grids must considerate the energy requested from the buildings
        // Energy available must considerate the energy sent to the grids
        double buildingRequestedEnergy = grid.getBuildingRequestedEnergy(); 
        double energySentToGrids = grid.getExpectedConsumption() - buildingRequestedEnergy;
        double availableEnergy = grid.getExpectedProduction() - energySentToGrids;
        // The energy is sent following a priority scheme
        for(Priority priority : Priority.values()){
            // Send energy to buildings in blackout before
            List<EnergyTransaction> blackoutSmartBuildings = grid.getBlackoutSmartBuildingsEnergyRequestsByPriority(priority);
            for(EnergyTransaction blackoutSmartBuildingEnergyRequest : blackoutSmartBuildings){
                double requestedEnergy = blackoutSmartBuildingEnergyRequest.getEnergyTransactionValue();
                String smartBuildingName = blackoutSmartBuildingEnergyRequest.getNodeName();

                Cable cable = grid.getCable(smartBuildingName);
                double neededEnergy = cable.getEnergyToSatisfyRequest(requestedEnergy);
                Map<String, Object> content = new HashMap<>();
                if(neededEnergy < availableEnergy){
                    // There is enough available energy to satisfy the current request
                    content.put(MessageUtil.GIVEN_ENERGY, neededEnergy);
                    availableEnergy -= neededEnergy;
                    customAgent.createAndSend(ACLMessage.INFORM, smartBuildingName, content, MessageUtil.CONVERSATION_ID_RESTORE_BUILDING + "-" + smartBuildingName);
                    grid.removeSmartBuildingWithoutPower(smartBuildingName);
                }else{
                    // There is not enough available energy to satisfy the current request
                    content.put(MessageUtil.GIVEN_ENERGY, -1.0); 
                    customAgent.createAndSend(ACLMessage.INFORM, smartBuildingName, content, MessageUtil.CONVERSATION_ID_RESTORE_BUILDING + "-" + smartBuildingName);
                }
            }

            // Send energy to buildings not in blackout
            List<EnergyTransaction> smartBuildingsEnergyRequests = grid.getSmartBuildingsEnergyRequestsByPriority(priority);
            for(EnergyTransaction smartBuildingsEnergyRequest : smartBuildingsEnergyRequests){
                double requestedEnergy = smartBuildingsEnergyRequest.getEnergyTransactionValue();
                String smartBuildingName = smartBuildingsEnergyRequest.getNodeName();
                
                Cable cable = grid.getCable(smartBuildingName);

                double neededEnergy = cable.getEnergyToSatisfyRequest(requestedEnergy);

                Map<String, Object> content = new HashMap<>();
                content.put(MessageUtil.OPERATION, MessageUtil.CONSUME);
                content.put(MessageUtil.REQUESTED_ENERGY, requestedEnergy);
                
                if(availableEnergy >= neededEnergy){
                    availableEnergy -= neededEnergy;
                    customAgent.createAndSend(ACLMessage.AGREE, smartBuildingName, content);
                }else{
                    customAgent.createAndSend(ACLMessage.REFUSE, smartBuildingName, content);
                    EnergyTransaction energyTransaction = new EnergyTransactionWithoutBattery(smartBuildingsEnergyRequest.getPriority(), neededEnergy, smartBuildingName, TransactionType.RECEIVE);
                    grid.addSmartBuildingWithoutPower(smartBuildingName, energyTransaction);
                }
                grid.removeEnergyRequest(smartBuildingName);
            }
        }
    }
}