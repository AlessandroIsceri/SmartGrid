package com.ii.smartgrid.behaviours.smartbuilding;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.agents.SmartBuildingAgent.SmartBuildingStatus;
import com.ii.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.model.Battery;
import com.ii.smartgrid.model.Cable;
import com.ii.smartgrid.model.building.BuildingPhotovoltaicSystem;
import com.ii.smartgrid.model.entities.SmartBuilding;
import com.ii.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.model.routing.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.model.routing.EnergyTransaction.TransactionType;
import com.ii.smartgrid.utils.EnergyMonitorUtil;
import com.ii.smartgrid.utils.MessageUtil;

import com.ii.smartgrid.utils.TimeUtils;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WaitForRestoreBehaviour extends CustomBehaviour{

    private enum Status {UPDATING_INTERNAL_ENERGY, RECEIVING_MESSAGES, FINISHED}
    private Status state = Status.UPDATING_INTERNAL_ENERGY;
    private SmartBuildingAgent smartBuildingAgent;
    
    public WaitForRestoreBehaviour(SmartBuildingAgent smartBuildingAgent){
        super(smartBuildingAgent);
        this.smartBuildingAgent = smartBuildingAgent;
    }

    @Override
    public void action() {
        switch (state) {
            case UPDATING_INTERNAL_ENERGY:
                updateInternalEnergy();
                break;
            case RECEIVING_MESSAGES:
                receiveMessages();
                break;
            default:
                break;
        }
    }

    private void updateInternalEnergy(){
        SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();
		double expectedProduction = smartBuilding.getExpectedProduction();
        String gridName = smartBuilding.getGridName();
        Battery battery = smartBuilding.getBattery();
		if(battery != null){
            Map<String, Object> content = new HashMap<>();
            if(smartBuilding.canBeRestored()){
                log("The building's battery reached 50% -> try to self restore");
                smartBuilding.restorePower(expectedProduction);
                smartBuildingAgent.setBuildingStatus(SmartBuildingStatus.LOSING_ENERGY);
                content.put(MessageUtil.BLACKOUT, false);
                content.put(MessageUtil.NEXT_TURN_EXPECTED_CONSUMPTION, smartBuilding.getNextTurnExpectedConsumption());
                state = Status.FINISHED;
            }else{
                battery.fillBattery(expectedProduction);
                content.put(MessageUtil.BLACKOUT, true);
                content.put(MessageUtil.NEXT_TURN_EXPECTED_CONSUMPTION, smartBuilding.getNextTurnExpectedConsumption());
                state = Status.RECEIVING_MESSAGES;
            }
            EnergyMonitorUtil.addTotalEnergyDemand(0, smartBuildingAgent.getCurTurn());
            EnergyMonitorUtil.addBuildingEnergyProduction(expectedProduction / TimeUtils.getTurnDurationHours(), smartBuildingAgent.getCurTurn());
            customAgent.createAndSend(ACLMessage.INFORM, gridName, content);
		}else{
            
            Map<String, Object> content = new HashMap<>();
            BuildingPhotovoltaicSystem buildingPhotovoltaicSystem = smartBuilding.getBuildingPhotovoltaicSystem();
            if(buildingPhotovoltaicSystem == null){
                log("The building's doesn't have neither a battery or a PV system -> Wait for energy from " + smartBuilding.getGridName());
                content.put(MessageUtil.BLACKOUT, true);
                content.put(MessageUtil.NEXT_TURN_EXPECTED_CONSUMPTION, smartBuilding.getNextTurnExpectedConsumption());

                EnergyMonitorUtil.addBuildingEnergyProduction(0, smartBuildingAgent.getCurTurn());
                EnergyMonitorUtil.addTotalEnergyDemand(0, smartBuildingAgent.getCurTurn());
                customAgent.createAndSend(ACLMessage.INFORM, gridName, content);
            }else{
                log("The building's doesn't have a battery -> Release PV energy to " + smartBuilding.getGridName());
                
                Cable cable = smartBuilding.getCable(gridName);
                double sentEnergy = cable.computeTransmittedPower(expectedProduction);

                EnergyMonitorUtil.addBuildingEnergyProduction(expectedProduction / TimeUtils.getTurnDurationHours(), smartBuildingAgent.getCurTurn());
                EnergyMonitorUtil.addTotalEnergyDemand(0, smartBuildingAgent.getCurTurn());

                EnergyTransaction energyTransaction = new EnergyTransactionWithoutBattery(smartBuilding.getPriority(), sentEnergy, customAgent.getLocalName(), TransactionType.SEND);
                
                ObjectMapper objectMapper = customAgent.getObjectMapper();
                JsonNode node = objectMapper.valueToTree(energyTransaction);
                
                content.put(MessageUtil.ENERGY_TRANSACTION, node);
                content.put(MessageUtil.BLACKOUT, true);
                content.put(MessageUtil.NEXT_TURN_EXPECTED_CONSUMPTION, smartBuilding.getNextTurnExpectedConsumption());

                customAgent.createAndSend(ACLMessage.REQUEST, gridName, content);
            }
            state = Status.RECEIVING_MESSAGES;
        }
    }

    private void receiveMessages() {
        MessageTemplate mtOR = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), 
                                                  MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        MessageTemplate mtAND = MessageTemplate.and(MessageTemplate.MatchConversationId(MessageUtil.CONVERSATION_ID_RESTORE_BUILDING + "-" + customAgent.getLocalName()),
												    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        MessageTemplate mt = MessageTemplate.or(mtOR, mtAND);
		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();           
            // Received a restore message from the grid
            if(receivedMsg.getPerformative() == ACLMessage.INFORM && receivedMsg.getConversationId().equals(MessageUtil.CONVERSATION_ID_RESTORE_BUILDING + "-" + customAgent.getLocalName())){
                Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
                log("Received a restore message");
                double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
                if(receivedEnergy >= 0){
                    smartBuilding.restorePower(receivedEnergy);
                    smartBuildingAgent.setBuildingStatus(SmartBuildingStatus.LOSING_ENERGY);
                }
            } else if (receivedMsg.getPerformative() == ACLMessage.AGREE) {
                log("Energy consumed");
            } else if (receivedMsg.getPerformative() == ACLMessage.REFUSE) {
                log("Energy not consumed");
            }
            state = Status.FINISHED;
		} else {
            block();
        }
        
    }

    @Override
    public boolean done() {
        return state == Status.FINISHED;
    }
    
}
