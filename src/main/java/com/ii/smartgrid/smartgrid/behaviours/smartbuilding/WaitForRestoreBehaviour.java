package com.ii.smartgrid.smartgrid.behaviours.smartbuilding;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent;
import com.ii.smartgrid.smartgrid.agents.SmartBuildingAgent.SmartBuildingStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.smartgrid.model.SmartBuilding;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WaitForRestoreBehaviour extends CustomBehaviour{

    private enum Status {UPDATING_INTERNAL_ENERGY, RECEIVING_MSGS, FINISHED}
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
            case RECEIVING_MSGS:
                receiveMsgs();
                break;
            default:
                break;
        }
    }

    private void updateInternalEnergy(){
        SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();
		
		double expectedProduction = smartBuilding.getExpectedProduction();
        log("PV: expectedProduction: " + expectedProduction);
		        
		Battery battery = smartBuilding.getBattery();

		if(battery != null){
            Map<String, Object> content = new HashMap<>();
            String gridName = smartBuilding.getGridName();
            if(smartBuilding.canBeRestored()){
                log("Ho almeno metà batteria piena, provo a ripartire");
                smartBuilding.restorePower(expectedProduction);
                smartBuildingAgent.setBuildingStatus(SmartBuildingStatus.LOSING_ENERGY);
                content.put(MessageUtil.BLACKOUT, false);
            }else{
                battery.fillBattery(expectedProduction);
                content.put(MessageUtil.BLACKOUT, true);
            }
            customAgent.createAndSend(ACLMessage.INFORM, gridName, content);
		}else{
            log("Non ho una batteria, ma ho dei pannelli, rilascio l'energia in rete");
            
            Map<String, Object> content = new HashMap<>();
            String gridName = smartBuilding.getGridName();
            Cable cable = smartBuilding.getCable(gridName);
            double sendedEnergy = cable.computeTransmittedPower(expectedProduction);
            EnergyTransaction energyTransaction = new EnergyTransactionWithoutBattery(smartBuilding.getPriority(), sendedEnergy, customAgent.getLocalName(), TransactionType.SEND);
            
            ObjectMapper objectMapper = customAgent.getObjectMapper();
            JsonNode node = objectMapper.valueToTree(energyTransaction); // include @JsonTypeInfo
            
            content.put(MessageUtil.ENERGY_TRANSACTION, node);
            content.put(MessageUtil.BLACKOUT, true);
            customAgent.createAndSend(ACLMessage.REQUEST, gridName, content);
        }
        state = Status.RECEIVING_MSGS;
    }

    private void receiveMsgs() {
        MessageTemplate mtOR = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), 
                                                  MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        MessageTemplate mtAND = MessageTemplate.and(MessageTemplate.MatchConversationId(MessageUtil.CONVERSATION_ID_RESTORE_BUILDING + "-" + customAgent.getLocalName()),
												    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        MessageTemplate mt = MessageTemplate.or(mtOR, mtAND);
		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();
            log("WAIT FOR RESTORE: HO RICEVUTO QUALCOSA: " + receivedMsg);
           
            if(receivedMsg.getPerformative() == ACLMessage.INFORM && receivedMsg.getConversationId().equals(MessageUtil.CONVERSATION_ID_RESTORE_BUILDING + "-" + customAgent.getLocalName())){
                Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
                log("Restore MSG received");
                double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
                if(receivedEnergy >= 0){
                    smartBuilding.restorePower(receivedEnergy);
                    smartBuildingAgent.setBuildingStatus(SmartBuildingStatus.LOSING_ENERGY);
                }
            }else if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                log("Energy consumed");
            }else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
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
