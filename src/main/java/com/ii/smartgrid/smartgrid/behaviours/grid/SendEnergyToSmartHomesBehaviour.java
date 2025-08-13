package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.CustomObject.Priority;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyToSmartHomesBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendEnergyToSmartHomesBehaviour(GridAgent gridAgent){
        super(gridAgent);
    }

    @Override
    public void action() {
        ((CustomAgent) myAgent).log("Started", BEHAVIOUR_NAME);
        Grid grid = ((GridAgent) myAgent).getGrid();

        //consumption: richieste da case in blackput 200 + richieste case 400 + energia inviata alle grid 300
        //production: energia da pp/case + energia da altre grid +1000
        // 1000-300 = 700
        double buildingRequestedEnergy = grid.getBuildingRequestedEnergy();  //+400
        double energySentToGrids = grid.getExpectedConsumption() - buildingRequestedEnergy;
        double availableEnergy = grid.getExpectedProduction() - energySentToGrids;
        for(Priority priority : Priority.values()){

            //blackout buildings before
            List<EnergyTransaction> blackoutSmartHomes = grid.getBlackoutSmartHomesEnergyRequestsByPriority(priority);
            for(EnergyTransaction blackoutSmartHomeEnergyRequest : blackoutSmartHomes){
                double requestedEnergy = blackoutSmartHomeEnergyRequest.getEnergyTransactionValue();
                String smartHomeName = blackoutSmartHomeEnergyRequest.getNodeName();

                Cable cable = grid.getCable(smartHomeName);
                double neededEnergy = cable.getEnergyToSatifyRequest(requestedEnergy);
                Map<String, Object> content = new HashMap<String, Object>();
                if(neededEnergy < availableEnergy){
                    content.put(MessageUtil.GIVEN_ENERGY, neededEnergy);
                    availableEnergy -= neededEnergy;
                    ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, smartHomeName, content, "restore-" + smartHomeName);
                    grid.removeSmartHomeWithoutPower(smartHomeName);
                }else{
                    content.put(MessageUtil.GIVEN_ENERGY, -1.0);
                    ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, smartHomeName, content, "restore-" + smartHomeName);
                    // grid.removeSmartHomeWithoutPower(smartHomeName); TODO: check
                }
            }




            List<EnergyTransaction> smartHomesEnergyRequests = grid.getSmartHomesEnergyRequestsByPriority(priority);
            for(EnergyTransaction smartHomesEnergyRequest : smartHomesEnergyRequests){
                double requestedEnergy = smartHomesEnergyRequest.getEnergyTransactionValue();
                String smartHomeName = smartHomesEnergyRequest.getNodeName();
                
                Cable cable = grid.getCable(smartHomeName);

                //loss:  x - cableResistance *  Math.pow(x / voltage, 2)) = requestedEnergy; 
                // x = requestedEnergy + cableResistanze * x^2/voltage^2
                // voltage^2*x = voltage^2*requestedEnergy + cableResistance * x^2 -> cableResistance* x^2 - voltage^2*x + voltage^2*requestedEnergy = 0
                // x = (-b ± √(b² - 4ac)) / (2a)
                // x = (+voltage^2 ± √(voltage^4 - 4 * cableResistance * voltage^2*requestedEnergy)) / (2*cableResistance)
                double neededEnergy = cable.getEnergyToSatifyRequest(requestedEnergy);

                Map<String, Object> content = new HashMap<String, Object>();
                content.put(MessageUtil.OPERATION, MessageUtil.CONSUME);
                content.put(MessageUtil.REQUESTED_ENERGY, requestedEnergy);
                
                if(availableEnergy >= neededEnergy){
                    availableEnergy -= neededEnergy;
                    ((CustomAgent) myAgent).createAndSend(ACLMessage.AGREE, smartHomeName, content);
                }else{
                    ((CustomAgent) myAgent).createAndSend(ACLMessage.REFUSE, smartHomeName, content);
                    EnergyTransaction energyTransaction = new EnergyTransactionWithoutBattery(smartHomesEnergyRequest.getPriority(), neededEnergy, smartHomeName, TransactionType.RECEIVE);
                    grid.addSmartHomeWithoutPower(smartHomeName, energyTransaction);
                }
                grid.removeEnergyRequest(smartHomeName);
            }
        }
        ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
    }
}