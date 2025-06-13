package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.beans.Customizer;
import java.util.HashMap;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyProducer;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WaitForRestoreBehaviour extends OneShotBehaviour{

    public WaitForRestoreBehaviour(SmartHome smartHome){
        super(smartHome);
    }

    @Override
    public void action() {

        List<EnergyProducer> energyProducers = ((SmartHome) myAgent).getEnergyProducers();
		
		//0:15 * 10 = 150 / 60 = 2
		int hour = (TimeUtils.getTurnDuration() * ((SmartHome) myAgent).getCurTurn()) / 60;
		double expectedProduction = 0;
		WeatherStatus curWeatherStatus = ((SmartHome) myAgent).getCurWeatherStatus();
		for(int i = 0; i < energyProducers.size(); i++) {
            //hproduction / 60 * turnDuration)
			expectedProduction += energyProducers.get(i).getHProduction(curWeatherStatus, hour) / 60 * TimeUtils.getTurnDuration();
		}
        ((SmartHome) myAgent).log("PV: expectedProduction: " + expectedProduction);
		
		Battery battery = ((SmartHome) myAgent).getBattery();

		if(battery != null){
			// battery.fillBattery(expectedProduction);
            double storedEnergy = battery.getStoredEnergy();
            ((SmartHome) myAgent).log("storedEnergy: " + storedEnergy);
            double capacity = battery.getMaxCapacity();
            if(capacity * 0.5 < storedEnergy + expectedProduction){
                ((SmartHome) myAgent).log("Ho almeno metà batteria piena, provo a ripartire");
                ((SmartHome) myAgent).restorePower(expectedProduction);
            }else{
                battery.fillBattery(expectedProduction);
            }
		}else{
            ((SmartHome) myAgent).log("Non ho una batteria, ma ho dei pannelli, rilascio l'energia in rete");
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID(((SmartHome) myAgent).getGridName(), AID.ISLOCALNAME));
            msg.setContent("{ \"operation\" : \"release\", \"energy\": " + expectedProduction + "}");
            myAgent.send(msg);
        }

        MessageTemplate mtOR = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), 
                                                  MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        MessageTemplate mtAND = MessageTemplate.and(MessageTemplate.MatchConversationId("restore-" + myAgent.getLocalName()),
												    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        MessageTemplate mt = MessageTemplate.or(mtOR, mtAND);
        ((SmartHome) myAgent).log("WAIT FOR RESTORE INIZIATA");
		ACLMessage receivedMsg = myAgent.receive(mt);
        ((SmartHome) myAgent).log("WAIT FOR RESTORE: HO RICEVUTO QUALCOSA: " + receivedMsg);
		if (receivedMsg != null) {
            if(receivedMsg.getPerformative() == ACLMessage.INFORM && receivedMsg.getConversationId().equals("restore-" + myAgent.getLocalName())){
                String receivedContent = receivedMsg.getContent();
                ObjectMapper objectMapper = new ObjectMapper();
                TypeReference<HashMap<String, Double>> typeRef = new TypeReference<HashMap<String, Double>>() {};
                HashMap<String, Double> jsonObject;
                ((SmartHome) myAgent).log("Restore MSG received");
                try {
                    jsonObject = objectMapper.readValue(receivedContent, typeRef);
                    double energy = jsonObject.get("energy");
                    ((SmartHome) myAgent).restorePower(energy);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }else if(receivedMsg.getPerformative() == ACLMessage.AGREE){
                ((SmartHome) myAgent).log("Energy released");
            }else if(receivedMsg.getPerformative() == ACLMessage.REFUSE){
                ((SmartHome) myAgent).log("Energy lost");
            }
		}
        ((SmartHome) myAgent).log("WAIT FOR RESTORE FINITA");
    }

	
    
}
