package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.beans.Customizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyProducer;
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

    private enum Status {UPDATING_INTERNAL_ENERGY, RECEIVING_MSGS, FINISHED}
    private Status state = Status.UPDATING_INTERNAL_ENERGY;

    
    public WaitForRestoreBehaviour(SmartHome smartHome){
        super(smartHome);
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
        List<EnergyProducer> energyProducers = ((SmartHome) myAgent).getEnergyProducers();
		
		//0:15 * 10 = 150 / 60 = 2
		int hour = (TimeUtils.getTurnDuration() * ((SmartHome) myAgent).getCurTurn()) / 60;
		double expectedProduction = 0;
		WeatherStatus curWeatherStatus = ((SmartHome) myAgent).getCurWeather();
		for(int i = 0; i < energyProducers.size(); i++) {
			expectedProduction += energyProducers.get(i).getHourlyProduction(curWeatherStatus, hour) / 60 * TimeUtils.getTurnDuration();
		}
        ((SmartHome) myAgent).log("PV: expectedProduction: " + expectedProduction);
		
		Battery battery = ((SmartHome) myAgent).getBattery();

		if(battery != null){
			// battery.fillBattery(expectedProduction);
            double storedEnergy = battery.getStoredEnergy();
            ((SmartHome) myAgent).log("storedEnergy: " + storedEnergy);
            double capacity = battery.getMaxCapacity();
            
            
            Map<String, Object> content = new HashMap<String, Object>();
            String gridName = ((SmartHome) myAgent).getGridName();
            String conversationId = "blackout-" + myAgent.getLocalName();
            if(capacity * 0.5 < storedEnergy + expectedProduction){
                ((SmartHome) myAgent).log("Ho almeno metà batteria piena, provo a ripartire");
                ((SmartHome) myAgent).restorePower(expectedProduction);
                content.put(MessageUtil.BLACKOUT, false);
            }else{
                battery.fillBattery(expectedProduction);
                content.put(MessageUtil.BLACKOUT, true);
            }
            ((SmartHome) myAgent).createAndSend(ACLMessage.INFORM, gridName, content, conversationId);
		}else{
            ((SmartHome) myAgent).log("Non ho una batteria, ma ho dei pannelli, rilascio l'energia in rete");
            
            Map<String, Object> content = new HashMap<String, Object>();
            String gridName = ((SmartHome) myAgent).getGridName();
            String conversationId = "release-"+myAgent.getLocalName();
            content.put(MessageUtil.OPERATION, MessageUtil.RELEASE);
            content.put(MessageUtil.RELEASED_ENERGY, expectedProduction);
            content.put(MessageUtil.BLACKOUT, true);
            ((SmartHome) myAgent).createAndSend(ACLMessage.INFORM, gridName, content, conversationId);
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
            ((SmartHome) myAgent).log("WAIT FOR RESTORE: HO RICEVUTO QUALCOSA: " + receivedMsg);
           
            if(receivedMsg.getPerformative() == ACLMessage.INFORM && receivedMsg.getConversationId().equals("restore-" + myAgent.getLocalName())){
                String receivedContent = receivedMsg.getContent();
                Map<String, Object> jsonObject = ((SmartHome) myAgent).convertAndReturnContent(receivedMsg);
                ((SmartHome) myAgent).log("Restore MSG received");
                double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
                if(receivedEnergy >= 0){
                    ((SmartHome) myAgent).restorePower(receivedEnergy);
                }
            }else if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                ((SmartHome) myAgent).log("Energy consumed");
            }else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                ((SmartHome) myAgent).log("Energy not consumed");
            }
            ((SmartHome) myAgent).log("WAIT FOR RESTORE FINITA");
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
