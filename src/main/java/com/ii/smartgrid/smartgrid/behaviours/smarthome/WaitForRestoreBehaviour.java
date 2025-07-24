package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.beans.Customizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.HomePhotovoltaicSystem;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WaitForRestoreBehaviour extends Behaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private enum Status {UPDATING_INTERNAL_ENERGY, RECEIVING_MSGS, FINISHED}
    private Status state = Status.UPDATING_INTERNAL_ENERGY;

    
    public WaitForRestoreBehaviour(SmartHomeAgent smartHomeAgent){
        super(smartHomeAgent);
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
        SmartHome smartHome = ((SmartHomeAgent) myAgent).getSmartHome();
        HomePhotovoltaicSystem homePhotovoltaicSystem = smartHome.getHomePhotovoltaicSystem();
		
		double expectedProduction = 0;
		WeatherStatus curWeatherStatus = ((SmartHomeAgent) myAgent).getCurWeather();
		// for(int i = 0; i < energyProducers.size(); i++) {
		expectedProduction += homePhotovoltaicSystem.getHourlyProduction(curWeatherStatus, ((SmartHomeAgent) myAgent).getCurTurn()) * TimeUtils.getTurnDurationHours();
		// }
        ((CustomAgent) myAgent).log("PV: expectedProduction: " + expectedProduction, BEHAVIOUR_NAME);
		        
		Battery battery = smartHome.getBattery();

		if(battery != null){
			// battery.fillBattery(expectedProduction);
            double storedEnergy = battery.getStoredEnergy();
            ((CustomAgent) myAgent).log("storedEnergy: " + storedEnergy, BEHAVIOUR_NAME);
            double capacity = battery.getMaxCapacity();
            
            
            Map<String, Object> content = new HashMap<String, Object>();
            String gridName = smartHome.getGridName();
            String conversationId = "blackout-" + myAgent.getLocalName();
            if(capacity * 0.5 < storedEnergy + expectedProduction){
                ((CustomAgent) myAgent).log("Ho almeno metà batteria piena, provo a ripartire", BEHAVIOUR_NAME);
                smartHome.restorePower(expectedProduction);
                content.put(MessageUtil.BLACKOUT, false);
            }else{
                battery.fillBattery(expectedProduction);
                content.put(MessageUtil.BLACKOUT, true);
            }
            ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, gridName, content, conversationId);
		}else{
            ((CustomAgent) myAgent).log("Non ho una batteria, ma ho dei pannelli, rilascio l'energia in rete", BEHAVIOUR_NAME);
            
            Map<String, Object> content = new HashMap<String, Object>();
            String gridName = smartHome.getGridName();
            String conversationId = "release-"+myAgent.getLocalName();
            content.put(MessageUtil.OPERATION, MessageUtil.RELEASE);
            content.put(MessageUtil.RELEASED_ENERGY, expectedProduction);
            content.put(MessageUtil.BLACKOUT, true);
            ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, gridName, content, conversationId);
        }
        state = Status.RECEIVING_MSGS;
    }

    private void receiveMsgs() {
        MessageTemplate mtOR = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), 
                                                  MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        MessageTemplate mtAND = MessageTemplate.and(MessageTemplate.MatchConversationId("restore-" + myAgent.getLocalName()),
												    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        MessageTemplate mt = MessageTemplate.or(mtOR, mtAND);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            SmartHome smartHome = ((SmartHomeAgent) myAgent).getSmartHome();
            ((CustomAgent) myAgent).log("WAIT FOR RESTORE: HO RICEVUTO QUALCOSA: " + receivedMsg, BEHAVIOUR_NAME);
           
            if(receivedMsg.getPerformative() == ACLMessage.INFORM && receivedMsg.getConversationId().equals("restore-" + myAgent.getLocalName())){
                String receivedContent = receivedMsg.getContent();
                Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
                ((CustomAgent) myAgent).log("Restore MSG received", BEHAVIOUR_NAME);
                double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
                if(receivedEnergy >= 0){
                    smartHome.restorePower(receivedEnergy);
                }
            }else if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                ((CustomAgent) myAgent).log("Energy consumed", BEHAVIOUR_NAME);
            }else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                ((CustomAgent) myAgent).log("Energy not consumed", BEHAVIOUR_NAME);
            }
            ((CustomAgent) myAgent).log("WAIT FOR RESTORE FINITA", BEHAVIOUR_NAME);
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
