package com.II.smartGrid.smartGrid.behaviours;

import java.beans.Customizer;
import java.util.HashMap;

import com.II.smartGrid.smartGrid.agents.SmartHome;
import com.II.smartGrid.smartGrid.tools.SimulationSettings.WeatherStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("restore-" + myAgent.getLocalName()),
												 MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ((SmartHome) myAgent).log("WAIT FOR RESTORE INIZIATA");
		ACLMessage receivedMsg = myAgent.receive(mt);
        ((SmartHome) myAgent).log("WAIT FOR RESTORE: HO RICEVUTO QUALCOSA: " + receivedMsg);
		if (receivedMsg != null) {
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
		}
        ((SmartHome) myAgent).log("WAIT FOR RESTORE FINITA");
    }

	
    
}
