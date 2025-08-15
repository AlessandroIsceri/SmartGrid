package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.CustomObject.Priority;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

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

        //consumption: richieste da case in blackput 200 + richieste case 400 + energia inviata alle grid 300
        //production: energia da pp/case + energia da altre grid +1000
        // 1000-300 = 700
        double buildingRequestedEnergy = grid.getBuildingRequestedEnergy();  //+400
        double energySentToGrids = grid.getExpectedConsumption() - buildingRequestedEnergy;
        double availableEnergy = grid.getExpectedProduction() - energySentToGrids;
        for(Priority priority : Priority.values()){
            //blackout buildings before
            List<EnergyTransaction> blackoutSmartBuildings = grid.getBlackoutSmartBuildingsEnergyRequestsByPriority(priority);
            for(EnergyTransaction blackoutSmartBuildingEnergyRequest : blackoutSmartBuildings){
                double requestedEnergy = blackoutSmartBuildingEnergyRequest.getEnergyTransactionValue();
                String smartBuildingName = blackoutSmartBuildingEnergyRequest.getNodeName();

                Cable cable = grid.getCable(smartBuildingName);
                double neededEnergy = cable.getEnergyToSatisfyRequest(requestedEnergy);
                Map<String, Object> content = new HashMap<>();
                if(neededEnergy < availableEnergy){
                    content.put(MessageUtil.GIVEN_ENERGY, neededEnergy);
                    availableEnergy -= neededEnergy;
                    customAgent.createAndSend(ACLMessage.INFORM, smartBuildingName, content, MessageUtil.CONVERSATION_ID_RESTORE_BUILDING + "-" + smartBuildingName);
                    grid.removeSmartBuildingWithoutPower(smartBuildingName);
                }else{
                    content.put(MessageUtil.GIVEN_ENERGY, -1.0);
                    customAgent.createAndSend(ACLMessage.INFORM, smartBuildingName, content, MessageUtil.CONVERSATION_ID_RESTORE_BUILDING + "-" + smartBuildingName);
                }
            }




            List<EnergyTransaction> smartBuildingsEnergyRequests = grid.getSmartBuildingsEnergyRequestsByPriority(priority);
            for(EnergyTransaction smartBuildingsEnergyRequest : smartBuildingsEnergyRequests){
                double requestedEnergy = smartBuildingsEnergyRequest.getEnergyTransactionValue();
                String smartBuildingName = smartBuildingsEnergyRequest.getNodeName();
                
                Cable cable = grid.getCable(smartBuildingName);

                //loss:  x - cableResistance *  Math.pow(x / voltage, 2)) = requestedEnergy; 
                // x = requestedEnergy + cableResistanze * x^2/voltage^2
                // voltage^2*x = voltage^2*requestedEnergy + cableResistance * x^2 -> cableResistance* x^2 - voltage^2*x + voltage^2*requestedEnergy = 0
                // x = (-b ± √(b² - 4ac)) / (2a)
                // x = (+voltage^2 ± √(voltage^4 - 4 * cableResistance * voltage^2*requestedEnergy)) / (2*cableResistance)
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