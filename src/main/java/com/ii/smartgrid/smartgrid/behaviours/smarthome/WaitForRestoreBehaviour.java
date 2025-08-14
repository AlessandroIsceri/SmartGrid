package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent.SmartHomeStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithoutBattery;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WaitForRestoreBehaviour extends CustomBehaviour{

    private enum Status {UPDATING_INTERNAL_ENERGY, RECEIVING_MSGS, FINISHED}
    private Status state = Status.UPDATING_INTERNAL_ENERGY;
    private SmartHomeAgent smartHomeAgent;
    
    public WaitForRestoreBehaviour(SmartHomeAgent smartHomeAgent){
        super(smartHomeAgent);
        this.smartHomeAgent = smartHomeAgent;
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
        SmartHome smartHome = smartHomeAgent.getSmartHome();
		
		WeatherStatus curWeatherStatus = smartHomeAgent.getCurWeather();
        int curTurn = smartHomeAgent.getCurTurn();
		double expectedProduction = smartHome.getExpectedProduction();
        log("PV: expectedProduction: " + expectedProduction);
		        
		Battery battery = smartHome.getBattery();

		if(battery != null){
            Map<String, Object> content = new HashMap<String, Object>();
            String gridName = smartHome.getGridName();
            // String conversationId = "blackout-" + customAgent.getLocalName();
            if(smartHome.canBeRestored(curTurn, curWeatherStatus)){
                log("Ho almeno metà batteria piena, provo a ripartire");
                smartHome.restorePower(expectedProduction);
                smartHomeAgent.setHomeStatus(SmartHomeStatus.LOSING_ENERGY);
                content.put(MessageUtil.BLACKOUT, false);
            }else{
                battery.fillBattery(expectedProduction);
                content.put(MessageUtil.BLACKOUT, true);
            }
            customAgent.createAndSend(ACLMessage.INFORM, gridName, content); //conversationId);
		}else{
            log("Non ho una batteria, ma ho dei pannelli, rilascio l'energia in rete");
            
            Map<String, Object> content = new HashMap<String, Object>();
            String gridName = smartHome.getGridName();
            // String conversationId = "release-"+customAgent.getLocalName();
            // content.put(MessageUtil.OPERATION, MessageUtil.RELEASE);

            // content.put(MessageUtil.RELEASED_ENERGY, expectedProduction);
            // content.put(MessageUtil.RELEASED_ENERGY, customAgent.updateEnergyValue(gridName, expectedProduction));
            Cable cable = smartHome.getCable(gridName);
            double sendedEnergy = cable.computeTransmittedPower(expectedProduction);
            EnergyTransaction energyTransaction = new EnergyTransactionWithoutBattery(smartHome.getPriority(), sendedEnergy, customAgent.getLocalName(), TransactionType.SEND);
            
            ObjectMapper objectMapper = customAgent.getObjectMapper();
            JsonNode node = objectMapper.valueToTree(energyTransaction); // include @JsonTypeInfo
            
            content.put(MessageUtil.ENERGY_TRANSACTION, node);
            content.put(MessageUtil.BLACKOUT, true);
            customAgent.createAndSend(ACLMessage.REQUEST, gridName, content);//conversationId);
        }
        state = Status.RECEIVING_MSGS;
    }

    private void receiveMsgs() {
        MessageTemplate mtOR = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), 
                                                  MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        MessageTemplate mtAND = MessageTemplate.and(MessageTemplate.MatchConversationId("restore-" + customAgent.getLocalName()),
												    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        MessageTemplate mt = MessageTemplate.or(mtOR, mtAND);
		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            SmartHome smartHome = smartHomeAgent.getSmartHome();
            log("WAIT FOR RESTORE: HO RICEVUTO QUALCOSA: " + receivedMsg);
           
            if(receivedMsg.getPerformative() == ACLMessage.INFORM && receivedMsg.getConversationId().equals("restore-" + customAgent.getLocalName())){
                String receivedContent = receivedMsg.getContent();
                Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
                log("Restore MSG received");
                double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
                if(receivedEnergy >= 0){
                    smartHome.restorePower(receivedEnergy);
                    smartHomeAgent.setHomeStatus(SmartHomeStatus.LOSING_ENERGY);
                }
            }else if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                log("Energy consumed");
            }else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                log("Energy not consumed");
            }
            //TODO REMOVE
            log("*****" + smartHome.toString());
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
